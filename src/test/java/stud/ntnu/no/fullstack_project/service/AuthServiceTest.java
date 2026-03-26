package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.no.fullstack_project.config.JwtService;
import stud.ntnu.no.fullstack_project.dto.auth.AdminSetupInfoResponse;
import stud.ntnu.no.fullstack_project.dto.auth.CompleteAdminSetupRequest;
import stud.ntnu.no.fullstack_project.dto.auth.MessageResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserService userService;

  @Mock
  private VerificationEmailService verificationEmailService;

  @InjectMocks
  private AuthService authService;

  private AppUser pendingAdmin;

  @BeforeEach
  void setUp() {
    Organization organization = new Organization();
    organization.setId(4L);
    organization.setName("North Peak Bistro");

    pendingAdmin = new AppUser();
    pendingAdmin.setId(9L);
    pendingAdmin.setUsername("ava");
    pendingAdmin.setFirstName("Ava");
    pendingAdmin.setLastName("Nilsen");
    pendingAdmin.setEmail("ava@example.com");
    pendingAdmin.setOrganization(organization);
    pendingAdmin.setRoles(Set.of(Role.ROLE_ADMIN));
    pendingAdmin.setAccountSetupToken("setup-token");
    pendingAdmin.setAccountSetupExpiresAt(LocalDateTime.now().plusDays(1));
    pendingAdmin.setEnabled(false);
    pendingAdmin.setEmailVerified(false);
  }

  @Test
  void getAdminSetupInfo_returnsPendingInviteDetails() {
    when(appUserRepository.findByAccountSetupToken("setup-token")).thenReturn(Optional.of(pendingAdmin));

    AdminSetupInfoResponse response = authService.getAdminSetupInfo("setup-token");

    assertEquals("ava@example.com", response.email());
    assertEquals("Ava", response.firstName());
    assertEquals("North Peak Bistro", response.organizationName());
  }

  @Test
  void completeAdminSetup_setsPasswordAndActivatesAccount() {
    when(appUserRepository.findByAccountSetupToken("setup-token")).thenReturn(Optional.of(pendingAdmin));
    when(passwordEncoder.encode("superSecret123")).thenReturn("encoded-password");

    MessageResponse response = authService.completeAdminSetup(
        new CompleteAdminSetupRequest("setup-token", "superSecret123")
    );

    assertEquals("Account setup complete. You can now log in.", response.message());
    assertEquals("encoded-password", pendingAdmin.getPassword());
    assertTrue(pendingAdmin.isEnabled());
    assertTrue(pendingAdmin.isEmailVerified());
    assertNull(pendingAdmin.getAccountSetupToken());
    assertNull(pendingAdmin.getAccountSetupExpiresAt());
    verify(appUserRepository).save(pendingAdmin);
  }

  @Test
  void completeAdminSetup_rejectsExpiredToken() {
    pendingAdmin.setAccountSetupExpiresAt(LocalDateTime.now().minusMinutes(1));
    when(appUserRepository.findByAccountSetupToken("setup-token")).thenReturn(Optional.of(pendingAdmin));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> authService.completeAdminSetup(new CompleteAdminSetupRequest("setup-token", "superSecret123"))
    );

    assertEquals("Setup token has expired", exception.getMessage());
    verify(appUserRepository, never()).save(any());
  }
}
