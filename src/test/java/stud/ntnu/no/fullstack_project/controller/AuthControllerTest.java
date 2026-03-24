package stud.ntnu.no.fullstack_project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.no.fullstack_project.dto.auth.AuthRequest;

/**
 * Integration tests for AuthController.
 * Uses full Spring context with H2 in-memory database.
 * Each test method that mutates data uses @DirtiesContext to avoid cross-contamination.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void registerReturns200() throws Exception {
    AuthRequest request = new AuthRequest("newuser", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Authentication successful"))
        .andExpect(jsonPath("$.user.username").value("newuser"));
  }

  @Test
  void loginWithSeededUserReturns200() throws Exception {
    // DataInitializer seeds an admin user with password "admin123"
    AuthRequest request = new AuthRequest("admin", "admin123");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Authentication successful"))
        .andExpect(jsonPath("$.user.username").value("admin"));
  }

  @Test
  void loginWithWrongPasswordReturns401() throws Exception {
    AuthRequest request = new AuthRequest("admin", "wrongpassword");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void statusWithoutTokenReturnsUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/auth/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(false))
        .andExpect(jsonPath("$.user").isEmpty());
  }

  @Test
  void registerWithShortUsernameReturnsBadRequest() throws Exception {
    // Username min 3 chars per @Size(min = 3) validation
    AuthRequest request = new AuthRequest("ab", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerWithShortPasswordReturnsBadRequest() throws Exception {
    // Password min 6 chars per @Size(min = 6) validation
    AuthRequest request = new AuthRequest("validuser", "short");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerDuplicateUsernameReturnsBadRequest() throws Exception {
    AuthRequest request = new AuthRequest("dupuser", "password123");

    // First registration should succeed
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Second registration with same username should fail
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logoutReturns200() throws Exception {
    mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isOk());
  }

  @Test
  void statusWithValidCookieReturnsAuthenticated() throws Exception {
    // Register a user to get a valid auth cookie
    AuthRequest request = new AuthRequest("cookieuser", "password123");

    // Capture the Set-Cookie header from registration
    String setCookie = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getHeader("Set-Cookie");

    // Extract the cookie name and value
    // Cookie format: auth-token=<jwt>; Path=/; ...
    assertNotNull(setCookie);
    String cookieHeader = setCookie.split(";")[0]; // "auth-token=<jwt>"

    mockMvc.perform(get("/api/auth/status")
            .header("Cookie", cookieHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(true))
        .andExpect(jsonPath("$.user.username").value("cookieuser"));
  }

  private void assertNotNull(Object obj) {
    if (obj == null) {
      throw new AssertionError("Expected non-null value");
    }
  }
}
