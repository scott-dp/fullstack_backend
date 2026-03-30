package stud.ntnu.no.fullstack_project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for authentication endpoints and their HTTP contracts.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.no.fullstack_project.dto.auth.EmailCodeLoginRequest;
import stud.ntnu.no.fullstack_project.dto.auth.EmailCodeRequest;
import stud.ntnu.no.fullstack_project.dto.auth.LoginRequest;
import stud.ntnu.no.fullstack_project.dto.auth.RegisterRequest;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Integration tests for AuthController.
 * Uses full Spring context with H2 in-memory database.
 * Each test method that mutates data uses @DirtiesContext to avoid cross-contamination.
 */
@SpringBootTest(properties = "app.mail.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AppUserRepository appUserRepository;

  @MockitoBean
  private JavaMailSender javaMailSender;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void registerReturns200() throws Exception {
    RegisterRequest request = new RegisterRequest("newuser", "newuser@example.com", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Registration successful. Check your email to verify your account before logging in."));
  }

  @Test
  void loginWithSeededUserReturns200() throws Exception {
    // DataInitializer seeds an admin user with password "admin123"
    LoginRequest request = new LoginRequest("admin@everest.no", "admin123");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Authentication successful"))
        .andExpect(jsonPath("$.user.username").value("admin"));
  }

  @Test
  void loginWithWrongPasswordReturns401() throws Exception {
    LoginRequest request = new LoginRequest("admin", "wrongpassword");

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
    RegisterRequest request = new RegisterRequest("ab", "short@example.com", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerWithShortPasswordReturnsBadRequest() throws Exception {
    // Password min 6 chars per @Size(min = 6) validation
    RegisterRequest request = new RegisterRequest("validuser", "valid@example.com", "short");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerDuplicateUsernameReturnsBadRequest() throws Exception {
    RegisterRequest request = new RegisterRequest("dupuser", "dup@example.com", "password123");

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
    LoginRequest request = new LoginRequest("admin", "admin123");

    String setCookie = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getHeader("Set-Cookie");

    assertNotNull(setCookie);
    String[] cookieParts = setCookie.split(";", 2)[0].split("=", 2);
    Cookie authCookie = new Cookie(cookieParts[0], cookieParts[1]);

    mockMvc.perform(get("/api/auth/status")
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(true))
        .andExpect(jsonPath("$.user.username").value("admin"));
  }

  @Test
  void loginWithUnverifiedUserReturnsBadRequest() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest("pendinguser", "pending@example.com", "password123");
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    LoginRequest loginRequest = new LoginRequest("pending@example.com", "password123");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Verify your email before logging in"));
  }

  @Test
  void verifyEmailActivatesRegisteredUser() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest("verifyme", "verifyme@example.com", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    String token = appUserRepository.findByEmail("verifyme@example.com")
        .orElseThrow()
        .getEmailVerificationToken();

    mockMvc.perform(get("/api/auth/verify").param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));

    LoginRequest loginRequest = new LoginRequest("verifyme@example.com", "password123");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("verifyme"));
  }

  @Test
  void requestEmailCodeForVerifiedUserReturns200() throws Exception {
    EmailCodeRequest request = new EmailCodeRequest("admin@everest.no");

    mockMvc.perform(post("/api/auth/email-code/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("A login code has been sent to your email."));
  }

  @Test
  void emailCodeLoginWorksForVerifiedUser() throws Exception {
    mockMvc.perform(post("/api/auth/email-code/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new EmailCodeRequest("admin@everest.no"))))
        .andExpect(status().isOk());

    String code = appUserRepository.findByEmail("admin@everest.no")
        .orElseThrow()
        .getEmailLoginCode();

    mockMvc.perform(post("/api/auth/email-code/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new EmailCodeLoginRequest("admin@everest.no", code))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("admin"));
  }

  @Test
  void registerRollsBackWhenVerificationEmailSendingFails() throws Exception {
    doThrow(new MailAuthenticationException("Authentication failed"))
        .when(javaMailSender)
        .send(any(org.springframework.mail.SimpleMailMessage.class));

    RegisterRequest request = new RegisterRequest("mailfail", "mailfail@example.com", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Failed to send verification email"));

    if (appUserRepository.existsByUsername("mailfail")) {
      throw new AssertionError("Expected registration rollback when email delivery fails");
    }
    if (appUserRepository.existsByEmail("mailfail@example.com")) {
      throw new AssertionError("Expected registration rollback when email delivery fails");
    }
  }

  private void assertNotNull(Object obj) {
    if (obj == null) {
      throw new AssertionError("Expected non-null value");
    }
  }
}
