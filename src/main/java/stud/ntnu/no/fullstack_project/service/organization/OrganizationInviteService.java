package stud.ntnu.no.fullstack_project.service.organization;

import java.time.LocalDateTime;
import java.util.HashSet;
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
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationInvite;
import stud.ntnu.no.fullstack_project.entity.auth.Role;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationInviteRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;
import stud.ntnu.no.fullstack_project.service.admin.UserService;

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

  /**
   * Parses the requested invitation role into the matching role enum.
   *
   * @param role raw invited role from the request
   * @return parsed role enum
   */
  private Role parseInviteRole(String role) {
    try {
      return Role.valueOf(role);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid invitation role: " + role);
    }
  }

  /**
   * Validates the requested invite expiry window against the allowed bounds.
   *
   * @param expiresInDays requested number of days before expiry
   * @return validated expiry window in days
   */
  private int validateExpiryDays(Integer expiresInDays) {
    if (expiresInDays == null || expiresInDays < MIN_EXPIRY_DAYS || expiresInDays > MAX_EXPIRY_DAYS) {
      throw new IllegalArgumentException(
          "Invitation expiry must be between " + MIN_EXPIRY_DAYS + " and " + MAX_EXPIRY_DAYS + " days"
      );
    }
    return expiresInDays;
  }

  /**
   * Resolves which organization the invite should target based on the caller's
   * role and organization membership.
   *
   * @param organizationId optional requested organization identifier
   * @param currentUser authenticated inviter
   * @return resolved target organization
   */
  private Organization resolveTargetOrganization(Long organizationId, AppUser currentUser) {
    if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
      if (currentUser.getOrganization() == null) {
        throw new IllegalArgumentException("Admin must belong to an organization to create invites");
      }
      Long adminOrganizationId = currentUser.getOrganization().getId();
      if (organizationId != null && !organizationId.equals(adminOrganizationId)) {
        throw new IllegalArgumentException("Admins can only invite users to their own organization");
      }
      return organizationRepository.findById(adminOrganizationId)
          .orElseThrow(() -> new IllegalArgumentException(
              "Organization not found with id: " + adminOrganizationId
          ));
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

  /**
   * Validates whether the current user is allowed to create the requested
   * invite for the resolved organization.
   *
   * @param currentUser authenticated inviter
   * @param roleToAssign invited role to be granted on acceptance
   * @param targetOrganization resolved invite target organization
   */
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

  /**
   * Resolves the final role set granted when an invite is accepted.
   *
   * @param invitedRole role encoded in the invite
   * @return roles to persist on the accepted user account
   */
  private Set<Role> resolveAcceptedRoles(Role invitedRole) {
    return switch (invitedRole) {
      case ROLE_MANAGER -> new HashSet<>(Set.of(Role.ROLE_MANAGER, Role.ROLE_STAFF));
      case ROLE_STAFF -> new HashSet<>(Set.of(Role.ROLE_STAFF));
      case ROLE_ADMIN -> throw new IllegalArgumentException("Admin invitations are not supported");
      case ROLE_SUPERADMIN -> throw new IllegalArgumentException("Superadmin invitations are not supported");
    };
  }

  /**
   * Reloads an authenticated user from the database to ensure the service
   * works with a managed entity instance.
   *
   * @param userId authenticated user identifier
   * @return persistent user entity
   */
  private AppUser loadUser(Long userId) {
    return appUserRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
  }

  /**
   * Maps an organization invite entity to the response returned by invite
   * create and list operations.
   *
   * @param invite persisted organization invite
   * @return serialized invite response
   */
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
