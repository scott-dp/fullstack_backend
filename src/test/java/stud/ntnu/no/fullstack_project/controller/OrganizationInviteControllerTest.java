package stud.ntnu.no.fullstack_project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.no.fullstack_project.dto.auth.LoginRequest;
import stud.ntnu.no.fullstack_project.dto.invite.CreateOrganizationInviteRequest;

/**
 * Integration tests for organization invite endpoints using the real Spring context and test profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationInviteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void adminCanCreateInviteWithoutExplicitOrganizationId() throws Exception {
    Cookie authCookie = loginAs("admin", "admin123");

    CreateOrganizationInviteRequest request =
        new CreateOrganizationInviteRequest("ROLE_STAFF", null, 7);

    mockMvc.perform(post("/api/organization-invites")
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.organizationId").value(1))
        .andExpect(jsonPath("$.organizationName").isNotEmpty())
        .andExpect(jsonPath("$.role").value("ROLE_STAFF"));
  }

  @Test
  void managerCannotCreateManagerInvite() throws Exception {
    Cookie authCookie = loginAs("manager", "manager123");

    CreateOrganizationInviteRequest request =
        new CreateOrganizationInviteRequest("ROLE_MANAGER", null, 7);

    mockMvc.perform(post("/api/organization-invites")
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Managers can only invite staff users"));
  }

  @Test
  void adminSeesCreatedInviteInList() throws Exception {
    Cookie authCookie = loginAs("admin", "admin123");

    CreateOrganizationInviteRequest request =
        new CreateOrganizationInviteRequest("ROLE_STAFF", null, 7);

    mockMvc.perform(post("/api/organization-invites")
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/organization-invites").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.organizationId == 1 && @.role == 'ROLE_STAFF')]").isNotEmpty());
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
