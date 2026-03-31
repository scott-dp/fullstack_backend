package stud.ntnu.no.fullstack_project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.no.fullstack_project.dto.auth.LoginRequest;
import stud.ntnu.no.fullstack_project.dto.user.AdminCreateUserRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;

/**
 * Integration tests for authenticated user-management endpoints against the test profile.
 */
@SpringBootTest(properties = "app.mail.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AppUserRepository appUserRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void adminCanCreateUserAndSeeItInOrganizationList() throws Exception {
    Cookie authCookie = loginAs("admin", "admin123");

    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "freshstaff",
        "password123",
        "Fresh",
        "Staff",
        "freshstaff@example.com",
        Set.of("ROLE_STAFF"),
        1L
    );

    mockMvc.perform(post("/api/users")
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("freshstaff"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_STAFF"));

    mockMvc.perform(get("/api/users").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.username=='freshstaff')]").isNotEmpty());
  }

  @Test
  void managerCannotCreateUsers() throws Exception {
    Cookie authCookie = loginAs("manager", "manager123");

    AdminCreateUserRequest request = new AdminCreateUserRequest(
        "blockeduser",
        "password123",
        "Blocked",
        "User",
        "blocked@example.com",
        Set.of("ROLE_STAFF"),
        1L
    );

    mockMvc.perform(post("/api/users")
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanSoftDeleteStaffUser() throws Exception {
    Cookie authCookie = loginAs("admin", "admin123");
    AppUser staff = appUserRepository.findByUsername("staff").orElseThrow();

    mockMvc.perform(delete("/api/users/{id}", staff.getId()).cookie(authCookie))
        .andExpect(status().isNoContent());

    AppUser updated = appUserRepository.findById(staff.getId()).orElseThrow();
    if (updated.isEnabled()) {
      throw new AssertionError("Expected staff user to be archived");
    }
    if (updated.getOrganization() != null) {
      throw new AssertionError("Expected archived user to be removed from its organization");
    }
    if (!updated.getRoles().isEmpty()) {
      throw new AssertionError("Expected archived user roles to be cleared");
    }
    if (updated.getEmail() != null) {
      throw new AssertionError("Expected archived user email to be cleared");
    }
  }

  private Cookie loginAs(String identifier, String password) throws Exception {
    String setCookie = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new LoginRequest(identifier, password))))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getHeader("Set-Cookie");

    if (setCookie == null) {
      throw new AssertionError("Expected authentication cookie");
    }

    String[] cookieParts = setCookie.split(";", 2)[0].split("=", 2);
    return new Cookie(cookieParts[0], cookieParts[1]);
  }
}
