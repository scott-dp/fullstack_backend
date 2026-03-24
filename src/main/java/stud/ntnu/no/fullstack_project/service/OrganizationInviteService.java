package stud.ntnu.no.fullstack_project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.invite.AcceptOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.CreateOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.OrganizationInviteResponse;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationInvite;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationInviteRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

/**
 * Service responsible for creating, listing, and accepting organization invites.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationInviteService {

  private static final int MIN_EXPIRY_DAYS = 1;
  private static final int MAX_EXPIRY_DAYS = 30;

  private final OrganizationInviteRepository organizationInviteRepository;
  private final OrganizationRepository organizationRepository;
  private final AppUserRepository appUserRepository;
  private final UserService userService;

  /**
   * Creates a new invite with access rules based on the inviter's role.
   *
   * @param request invite request payload
   * @param currentUser authenticated inviter
   * @return created invite response
   */
  @Transactional
  public OrganizationInviteResponse createInvite(
      CreateOrganizationInviteRequest request,
      AppUser currentUser
  ) {
    Role roleToAssign = parseInviteRole(request.role());
    int expiresInDays = validateExpiryDays(request.expiresInDays());

    Organization targetOrganization = resolveTargetOrganization(request.organizationId(), currentUser);
    validateCreatePermission(currentUser, roleToAssign, targetOrganization);

    OrganizationInvite invite = new OrganizationInvite();
    invite.setToken(UUID.randomUUID().toString());
    invite.setOrganization(targetOrganization);
    invite.setRoleToAssign(roleToAssign);
    invite.setCreatedBy(loadUser(currentUser.getId()));
    invite.setExpiresAt(LocalDateTime.now().plusDays(expiresInDays));
    invite.setRevoked(false);

    OrganizationInvite saved = organizationInviteRepository.save(invite);
    log.info(
        "Created organization invite id={} orgId={} role={} by user={}",
        saved.getId(),
        saved.getOrganization().getId(),
        saved.getRoleToAssign(),
        currentUser.getUsername()
    );
    return mapToResponse(saved);
  }

  /**
   * Lists invites visible to the current user.
   *
   * <p>Admins see all invites. Managers see invites for their own organization.</p>
   *
   * @param currentUser authenticated caller
   * @return invite list ordered newest first
   */
  public List<OrganizationInviteResponse> listVisibleInvites(AppUser currentUser) {
    List<OrganizationInvite> invites;

    if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
      invites = organizationInviteRepository.findAllByOrderByCreatedAtDesc();
    } else if (currentUser.getRoles().contains(Role.ROLE_MANAGER)) {
      if (currentUser.getOrganization() == null) {
        return List.of();
      }
      invites = organizationInviteRepository.findByOrganizationIdOrderByCreatedAtDesc(
          currentUser.getOrganization().getId()
      );
    } else {
      throw new IllegalArgumentException("Only admins and managers can view organization invites");
    }

    return invites.stream().map(this::mapToResponse).toList();
  }

  /**
   * Accepts an invite for the authenticated user and assigns the invite's role and organization.
   *
   * @param request invitation token payload
   * @param currentUser authenticated user accepting the invite
   * @return updated user profile response
   */
  @Transactional
  public CurrentUserResponse acceptInvite(
      AcceptOrganizationInviteRequest request,
      AppUser currentUser
  ) {
    AppUser persistentUser = loadUser(currentUser.getId());

    if (persistentUser.getOrganization() != null) {
      throw new IllegalArgumentException("User already belongs to an organization");
    }

    OrganizationInvite invite = organizationInviteRepository.findByToken(request.token().trim())
        .orElseThrow(() -> new IllegalArgumentException("Invitation token was not found"));

    if (invite.isRevoked()) {
      throw new IllegalArgumentException("Invitation has been revoked");
    }
    if (invite.isAccepted()) {
      throw new IllegalArgumentException("Invitation has already been accepted");
    }
    if (invite.isExpired()) {
      throw new IllegalArgumentException("Invitation has expired");
    }

    persistentUser.setOrganization(invite.getOrganization());
    persistentUser.setRoles(resolveAcceptedRoles(invite.getRoleToAssign()));
    appUserRepository.save(persistentUser);

    invite.setAcceptedAt(LocalDateTime.now());
    invite.setAcceptedBy(persistentUser);
    organizationInviteRepository.save(invite);

    log.info(
        "User {} accepted organization invite id={} orgId={} role={}",
        persistentUser.getUsername(),
        invite.getId(),
        invite.getOrganization().getId(),
        invite.getRoleToAssign()
    );

    return userService.mapToResponse(persistentUser);
  }

  private Role parseInviteRole(String role) {
    try {
      return Role.valueOf(role);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid invitation role: " + role);
    }
  }

  private int validateExpiryDays(Integer expiresInDays) {
    if (expiresInDays == null || expiresInDays < MIN_EXPIRY_DAYS || expiresInDays > MAX_EXPIRY_DAYS) {
      throw new IllegalArgumentException(
          "Invitation expiry must be between " + MIN_EXPIRY_DAYS + " and " + MAX_EXPIRY_DAYS + " days"
      );
    }
    return expiresInDays;
  }

  private Organization resolveTargetOrganization(Long organizationId, AppUser currentUser) {
    if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
      if (organizationId == null) {
        throw new IllegalArgumentException("organizationId is required for admin-created invites");
      }
      return organizationRepository.findById(organizationId)
          .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + organizationId));
    }

    if (!currentUser.getRoles().contains(Role.ROLE_MANAGER)) {
      throw new IllegalArgumentException("Only admins and managers can create organization invites");
    }

    if (currentUser.getOrganization() == null) {
      throw new IllegalArgumentException("Manager must belong to an organization to create invites");
    }

    return organizationRepository.findById(currentUser.getOrganization().getId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Organization not found with id: " + currentUser.getOrganization().getId()
        ));
  }

  private void validateCreatePermission(AppUser currentUser, Role roleToAssign, Organization targetOrganization) {
    if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
      if (roleToAssign == Role.ROLE_ADMIN) {
        throw new IllegalArgumentException("Admin invitations are not supported");
      }
      return;
    }

    if (currentUser.getRoles().contains(Role.ROLE_MANAGER)) {
      if (roleToAssign != Role.ROLE_STAFF) {
        throw new IllegalArgumentException("Managers can only invite staff users");
      }
      if (currentUser.getOrganization() == null
          || !currentUser.getOrganization().getId().equals(targetOrganization.getId())) {
        throw new IllegalArgumentException("Managers can only invite users to their own organization");
      }
      return;
    }

    throw new IllegalArgumentException("Only admins and managers can create organization invites");
  }

  private Set<Role> resolveAcceptedRoles(Role invitedRole) {
    return switch (invitedRole) {
      case ROLE_MANAGER -> Set.of(Role.ROLE_MANAGER, Role.ROLE_STAFF);
      case ROLE_STAFF -> Set.of(Role.ROLE_STAFF);
      case ROLE_ADMIN -> throw new IllegalArgumentException("Admin invitations are not supported");
    };
  }

  private AppUser loadUser(Long userId) {
    return appUserRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
  }

  private OrganizationInviteResponse mapToResponse(OrganizationInvite invite) {
    return new OrganizationInviteResponse(
        invite.getId(),
        invite.getToken(),
        invite.getOrganization().getId(),
        invite.getOrganization().getName(),
        invite.getRoleToAssign().name(),
        invite.getCreatedBy().getUsername(),
        invite.getCreatedAt(),
        invite.getExpiresAt(),
        invite.isAccepted(),
        invite.getAcceptedBy() != null ? invite.getAcceptedBy().getUsername() : null,
        invite.isRevoked()
    );
  }
}
