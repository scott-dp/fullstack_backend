package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.no.fullstack_project.config.SecurityUtil;
import stud.ntnu.no.fullstack_project.dto.user.AdminCreateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.dto.user.UpdateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.UserSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityUtil securityUtil;

  @InjectMocks
  private UserService userService;

  private AppUser testUser;
  private Organization testOrg;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encoded");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setEmail("test@example.com");
    testUser.setOrganization(testOrg);
    testUser.setRoles(new HashSet<>(Set.of(Role.ROLE_STAFF)));
  }

  // --- getCurrentUser tests ---

  @Test
  void getCurrentUser_returnsCurrentUserResponse() {
    when(securityUtil.getCurrentUsername()).thenReturn("testuser");
    when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    CurrentUserResponse response = userService.getCurrentUser();

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("testuser", response.username());
    assertEquals("Test", response.firstName());
    assertEquals("User", response.lastName());
    assertEquals("test@example.com", response.email());
    assertEquals(1L, response.organizationId());
    assertEquals("Test Org", response.organizationName());
    assertTrue(response.roles().contains("ROLE_STAFF"));
  }

  @Test
  void getCurrentUser_nonExistentUser_throws() {
    when(securityUtil.getCurrentUsername()).thenReturn("nonexistent");
    when(appUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.getCurrentUser());
    assertTrue(ex.getMessage().contains("not found"));
  }

  // --- updateProfile tests ---

  @Test
  void updateProfile_updatesFields() {
    UpdateUserRequest request = new UpdateUserRequest("NewFirst", "NewLast", "new@example.com");

    when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

    CurrentUserResponse response = userService.updateProfile(request, testUser);

    assertEquals("NewFirst", response.firstName());
    assertEquals("NewLast", response.lastName());
    assertEquals("new@example.com", response.email());
    verify(appUserRepository).save(testUser);
  }

  @Test
  void updateProfile_sameEmail_doesNotCheckDuplicate() {
    UpdateUserRequest request = new UpdateUserRequest("NewFirst", null, "test@example.com");

    when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

    CurrentUserResponse response = userService.updateProfile(request, testUser);

    assertEquals("NewFirst", response.firstName());
    verify(appUserRepository, never()).existsByEmail(anyString());
  }

  @Test
  void updateProfile_duplicateEmail_throws() {
    UpdateUserRequest request = new UpdateUserRequest(null, null, "taken@example.com");

    when(appUserRepository.existsByEmail("taken@example.com")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.updateProfile(request, testUser));
    assertTrue(ex.getMessage().contains("already in use"));
    verify(appUserRepository, never()).save(any());
  }

  @Test
  void updateProfile_nullFields_doesNotOverwrite() {
    UpdateUserRequest request = new UpdateUserRequest(null, null, null);

    when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

    CurrentUserResponse response = userService.updateProfile(request, testUser);

    assertEquals("Test", response.firstName());
    assertEquals("User", response.lastName());
    assertEquals("test@example.com", response.email());
  }

  // --- listUsersInOrganization tests ---

  @Test
  void listUsersInOrganization_returnsList() {
    AppUser user2 = new AppUser();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setPassword("encoded");
    user2.setFirstName("Jane");
    user2.setLastName("Doe");
    user2.setOrganization(testOrg);
    user2.setRoles(new HashSet<>(Set.of(Role.ROLE_MANAGER)));

    when(appUserRepository.findByOrganizationIdAndEnabledTrue(1L)).thenReturn(List.of(testUser, user2));

    List<UserSummaryResponse> result = userService.listUsersInOrganization(1L);

    assertEquals(2, result.size());
    assertEquals("testuser", result.get(0).username());
    assertEquals("user2", result.get(1).username());
  }

  @Test
  void listUsersInOrganization_emptyOrg_returnsEmpty() {
    when(appUserRepository.findByOrganizationIdAndEnabledTrue(99L)).thenReturn(List.of());

    List<UserSummaryResponse> result = userService.listUsersInOrganization(99L);

    assertTrue(result.isEmpty());
  }

  // --- createUser tests ---

  @Test
  void createUser_validInput_createsUser() {
    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "newuser", "password123", "New", "User", "new@example.com",
        Set.of("ROLE_STAFF"), 1L
    );

    when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
    when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
      AppUser saved = invocation.getArgument(0);
      saved.setId(10L);
      return saved;
    });

    CurrentUserResponse response = userService.createUser(request);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("newuser", response.username());
    assertEquals("New", response.firstName());
    assertEquals("new@example.com", response.email());
    assertEquals(1L, response.organizationId());
    assertTrue(response.roles().contains("ROLE_STAFF"));
    verify(passwordEncoder).encode("password123");
  }

  @Test
  void createUser_duplicateUsername_throws() {
    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "existing", "password123", "F", "L", "e@e.com",
        Set.of("ROLE_STAFF"), 1L
    );

    when(appUserRepository.existsByUsername("existing")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.createUser(request));
    assertTrue(ex.getMessage().contains("already taken"));
    verify(appUserRepository, never()).save(any());
  }

  @Test
  void createUser_duplicateEmail_throws() {
    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "unique", "password123", "F", "L", "taken@e.com",
        Set.of("ROLE_STAFF"), 1L
    );

    when(appUserRepository.existsByUsername("unique")).thenReturn(false);
    when(appUserRepository.existsByEmail("taken@e.com")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.createUser(request));
    assertTrue(ex.getMessage().contains("already in use"));
  }

  @Test
  void createUser_nonExistentOrg_throws() {
    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "user", "password123", "F", "L", null,
        Set.of("ROLE_STAFF"), 999L
    );

    when(appUserRepository.existsByUsername("user")).thenReturn(false);
    when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
  }

  @Test
  void createUser_invalidRole_throws() {
    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "user", "password123", "F", "L", null,
        Set.of("INVALID_ROLE"), 1L
    );

    when(appUserRepository.existsByUsername("user")).thenReturn(false);
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));

    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
  }

  @Test
  void deleteUser_softDeletesStaffUser() {
    AppUser adminUser = new AppUser();
    adminUser.setId(9L);
    adminUser.setUsername("admin");
    adminUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN)));

    when(appUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

    userService.deleteUser(1L, adminUser);

    assertFalse(testUser.isEnabled());
    assertNull(testUser.getOrganization());
    assertNull(testUser.getEmail());
    assertTrue(testUser.getRoles().isEmpty());
    verify(appUserRepository).save(testUser);
  }

  @Test
  void deleteUser_cannotDeleteSelf() {
    AppUser adminUser = new AppUser();
    adminUser.setId(1L);
    adminUser.setUsername("admin");
    adminUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN)));

    when(appUserRepository.findById(1L)).thenReturn(Optional.of(testUser));

    assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(1L, adminUser));
  }
}
