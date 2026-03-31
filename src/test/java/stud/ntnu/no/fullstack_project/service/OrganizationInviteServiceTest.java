package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Service tests for organization invite creation, acceptance, and org-scoped rules.
 */
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.invite.AcceptOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.CreateOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.OrganizationInviteResponse;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationInvite;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.auth.Role;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationInviteRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;
import stud.ntnu.no.fullstack_project.service.admin.UserService;
import stud.ntnu.no.fullstack_project.service.organization.OrganizationInviteService;

@ExtendWith(MockitoExtension.class)
class OrganizationInviteServiceTest {

  @Mock
  private OrganizationInviteRepository organizationInviteRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private OrganizationInviteService organizationInviteService;

  private AppUser adminUser;
  private AppUser managerUser;
  private AppUser staffUser;
  private Organization organization;

  @BeforeEach
  void setUp() {
    organization = new Organization();
    organization.setId(10L);
    organization.setName("Everest Sushi & Fusion");
    organization.setType(OrganizationType.RESTAURANT);

    adminUser = new AppUser();
    adminUser.setId(1L);
    adminUser.setUsername("admin");
    adminUser.setRoles(Set.of(Role.ROLE_ADMIN));
    adminUser.setOrganization(organization);

    managerUser = new AppUser();
    managerUser.setId(2L);
    managerUser.setUsername("manager");
    managerUser.setRoles(Set.of(Role.ROLE_MANAGER));
    managerUser.setOrganization(organization);

    staffUser = new AppUser();
    staffUser.setId(3L);
    staffUser.setUsername("staff");
    staffUser.setRoles(Set.of(Role.ROLE_STAFF));
  }

  @Test
  void createInvite_allowsAdminToCreateManagerInvite() {
    CreateOrganizationInviteRequest request = new CreateOrganizationInviteRequest("ROLE_MANAGER", null, 7);
    when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
    when(appUserRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(organizationInviteRepository.save(any(OrganizationInvite.class))).thenAnswer(invocation -> {
      OrganizationInvite invite = invocation.getArgument(0);
      invite.setId(5L);
      invite.setCreatedAt(LocalDateTime.now());
      return invite;
    });

    OrganizationInviteResponse response = organizationInviteService.createInvite(request, adminUser);

    assertNotNull(response.token());
    assertEquals("ROLE_MANAGER", response.role());
    assertEquals("Everest Sushi & Fusion", response.organizationName());
    assertFalse(response.accepted());
  }

  @Test
  void createInvite_rejectsAdminInviteForDifferentOrganization() {
    CreateOrganizationInviteRequest request = new CreateOrganizationInviteRequest("ROLE_MANAGER", 11L, 7);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> organizationInviteService.createInvite(request, adminUser)
    );

    assertEquals("Admins can only invite users to their own organization", exception.getMessage());
    verify(organizationRepository, never()).findById(any());
  }

  @Test
  void createInvite_rejectsManagerInviteForManagerRole() {
    CreateOrganizationInviteRequest request = new CreateOrganizationInviteRequest("ROLE_MANAGER", null, 7);
    when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> organizationInviteService.createInvite(request, managerUser)
    );

    assertEquals("Managers can only invite staff users", exception.getMessage());
  }

  @Test
  void acceptInvite_assignsOrganizationAndRoles() {
    OrganizationInvite invite = new OrganizationInvite();
    invite.setId(9L);
    invite.setToken("token-123");
    invite.setOrganization(organization);
    invite.setRoleToAssign(Role.ROLE_MANAGER);
    invite.setCreatedBy(adminUser);
    invite.setExpiresAt(LocalDateTime.now().plusDays(5));

    CurrentUserResponse updatedUser = new CurrentUserResponse(
        3L,
        "staff",
        null,
        null,
        null,
        Set.of("ROLE_MANAGER", "ROLE_STAFF"),
        10L,
        "Everest Sushi & Fusion"
    );

    when(appUserRepository.findById(3L)).thenReturn(Optional.of(staffUser));
    when(organizationInviteRepository.findByToken("token-123")).thenReturn(Optional.of(invite));
    when(userService.mapToResponse(staffUser)).thenReturn(updatedUser);

    CurrentUserResponse response = organizationInviteService.acceptInvite(
        new AcceptOrganizationInviteRequest("token-123"),
        staffUser
    );

    assertEquals(10L, response.organizationId());
    assertTrue(staffUser.getRoles().contains(Role.ROLE_MANAGER));
    assertTrue(staffUser.getRoles().contains(Role.ROLE_STAFF));
    assertNotNull(invite.getAcceptedAt());
    assertEquals(staffUser, invite.getAcceptedBy());
    verify(appUserRepository).save(staffUser);
    verify(organizationInviteRepository).save(invite);
  }

  @Test
  void acceptInvite_rejectsUsersThatAlreadyBelongToOrganization() {
    staffUser.setOrganization(organization);
    when(appUserRepository.findById(3L)).thenReturn(Optional.of(staffUser));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> organizationInviteService.acceptInvite(new AcceptOrganizationInviteRequest("token-123"), staffUser)
    );

    assertEquals("User already belongs to an organization", exception.getMessage());
    verify(organizationInviteRepository, never()).findByToken(any());
  }

  @Test
  void acceptInvite_rejectsExpiredInvites() {
    OrganizationInvite invite = new OrganizationInvite();
    invite.setToken("expired");
    invite.setOrganization(organization);
    invite.setRoleToAssign(Role.ROLE_STAFF);
    invite.setCreatedBy(managerUser);
    invite.setExpiresAt(LocalDateTime.now().minusDays(1));

    when(appUserRepository.findById(3L)).thenReturn(Optional.of(staffUser));
    when(organizationInviteRepository.findByToken("expired")).thenReturn(Optional.of(invite));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> organizationInviteService.acceptInvite(new AcceptOrganizationInviteRequest("expired"), staffUser)
    );

    assertEquals("Invitation has expired", exception.getMessage());
  }
}
