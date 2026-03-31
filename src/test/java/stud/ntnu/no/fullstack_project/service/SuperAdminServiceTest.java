package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Service tests for superadmin provisioning and organization-admin lifecycle flows.
 */
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.no.fullstack_project.dto.superadmin.CreateOrganizationAdminRequest;
import stud.ntnu.no.fullstack_project.dto.superadmin.OrganizationAdminSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.auth.Role;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;
import stud.ntnu.no.fullstack_project.service.admin.SuperAdminService;
import stud.ntnu.no.fullstack_project.service.auth.VerificationEmailService;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private VerificationEmailService verificationEmailService;

  @InjectMocks
  private SuperAdminService superAdminService;

  private Organization organization;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(superAdminService, "frontendUrl", "http://localhost:5173");

    organization = new Organization();
    organization.setId(10L);
    organization.setName("North Peak Bistro");
    organization.setType(OrganizationType.RESTAURANT);
  }

  @Test
  void createOrganizationWithAdmin_createsOrganizationAndPendingAdmin() {
    CreateOrganizationAdminRequest request = new CreateOrganizationAdminRequest(
        "North Peak Bistro",
        "123456789",
        "RESTAURANT",
        "Ava",
        "Nilsen",
        "ava@example.com"
    );

    when(appUserRepository.existsByEmail("ava@example.com")).thenReturn(false);
    when(organizationRepository.existsByOrganizationNumber("123456789")).thenReturn(false);
    when(appUserRepository.existsByUsername("ava")).thenReturn(false);
    when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
      Organization saved = invocation.getArgument(0);
      saved.setId(10L);
      return saved;
    });
    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-temp-password");
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
      AppUser saved = invocation.getArgument(0);
      saved.setId(5L);
      return saved;
    });

    OrganizationAdminSummaryResponse response = superAdminService.createOrganizationWithAdmin(request);

    assertEquals("North Peak Bistro", response.organizationName());
    assertEquals("ava@example.com", response.email());
    assertTrue(response.setupPending());
    assertFalse(response.active());
    verify(verificationEmailService).sendAdminSetupEmail(
        any(String.class),
        any(String.class),
        any(String.class)
    );
  }

  @Test
  void createOrganizationWithAdmin_duplicateEmail_throws() {
    CreateOrganizationAdminRequest request = new CreateOrganizationAdminRequest(
        "North Peak Bistro",
        null,
        "RESTAURANT",
        "Ava",
        "Nilsen",
        "ava@example.com"
    );

    when(appUserRepository.existsByEmail("ava@example.com")).thenReturn(true);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> superAdminService.createOrganizationWithAdmin(request)
    );

    assertEquals("Email is already in use", exception.getMessage());
    verify(organizationRepository, never()).save(any());
  }

  @Test
  void listOrganizationAdmins_returnsMappedResponses() {
    AppUser admin = new AppUser();
    admin.setId(5L);
    admin.setUsername("ava");
    admin.setFirstName("Ava");
    admin.setLastName("Nilsen");
    admin.setEmail("ava@example.com");
    admin.setOrganization(organization);
    admin.setRoles(Set.of(Role.ROLE_ADMIN));
    admin.setEnabled(true);

    when(appUserRepository.findAllByRole(Role.ROLE_ADMIN)).thenReturn(List.of(admin));

    List<OrganizationAdminSummaryResponse> result = superAdminService.listOrganizationAdmins();

    assertEquals(1, result.size());
    assertEquals("ava@example.com", result.getFirst().email());
    assertEquals("North Peak Bistro", result.getFirst().organizationName());
  }

  @Test
  void archiveOrganizationAdmin_disablesAdminAccount() {
    AppUser currentSuperAdmin = new AppUser();
    currentSuperAdmin.setId(1L);
    currentSuperAdmin.setRoles(Set.of(Role.ROLE_SUPERADMIN));

    AppUser admin = new AppUser();
    admin.setId(5L);
    admin.setUsername("ava");
    admin.setRoles(Set.of(Role.ROLE_ADMIN));
    admin.setEnabled(true);
    admin.setAccountSetupToken("setup-token");

    when(appUserRepository.findById(5L)).thenReturn(Optional.of(admin));

    superAdminService.archiveOrganizationAdmin(5L, currentSuperAdmin);

    assertFalse(admin.isEnabled());
    assertNull(admin.getAccountSetupToken());
    verify(appUserRepository).save(admin);
  }

  @Test
  void archiveOrganizationAdmin_rejectsNonAdminTargets() {
    AppUser currentSuperAdmin = new AppUser();
    currentSuperAdmin.setId(1L);
    currentSuperAdmin.setRoles(Set.of(Role.ROLE_SUPERADMIN));

    AppUser manager = new AppUser();
    manager.setId(7L);
    manager.setRoles(Set.of(Role.ROLE_MANAGER));

    when(appUserRepository.findById(7L)).thenReturn(Optional.of(manager));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> superAdminService.archiveOrganizationAdmin(7L, currentSuperAdmin)
    );

    assertEquals("Only organization admin accounts can be archived here", exception.getMessage());
    verify(appUserRepository, never()).save(any());
  }
}
