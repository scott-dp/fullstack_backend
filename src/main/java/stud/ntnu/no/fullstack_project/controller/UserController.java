package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.user.AdminCreateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.dto.user.UpdateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.UserSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.UserService;

/**
 * REST controller for user management operations.
 *
 * <p>Provides endpoints to retrieve and update the authenticated user's profile,
 * list organization members, and (for admins) create new user accounts.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(
      summary = "Get the currently authenticated user",
      description = "Returns the full profile of the user identified by the JWT cookie."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user profile retrieved",
          content = @Content(schema = @Schema(implementation = CurrentUserResponse.class)))
  })
  public ResponseEntity<CurrentUserResponse> getCurrentUser() {
    log.info("Fetching current user profile");
    return ResponseEntity.ok(userService.getCurrentUser());
  }

  @PutMapping("/me")
  @Operation(
      summary = "Update the currently authenticated user's profile",
      description = "Updates profile fields (firstName, lastName, email) for the authenticated user."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profile updated successfully",
          content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or email already in use",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<CurrentUserResponse> updateProfile(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Profile fields to update.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateUserRequest.class),
              examples = @ExampleObject(name = "Update profile", value = """
                  {"firstName": "Scott", "lastName": "Liddell", "email": "scott@example.com"}
                  """)))
      @Valid @RequestBody UpdateUserRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Updating profile for user={}", currentUser.getUsername());
    return ResponseEntity.ok(userService.updateProfile(request, currentUser));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "List users in the current user's organization",
      description = "Returns a summary list of all users belonging to the same organization. "
          + "Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<UserSummaryResponse>> listUsers(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing users for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        userService.listUsersInOrganization(currentUser.getOrganization().getId())
    );
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Admin creates a new user",
      description = "Creates a new user account with specified roles and organization. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User created successfully",
          content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed, username/email taken, or invalid role/org",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<CurrentUserResponse> createUser(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "New user account details.",
          required = true,
          content = @Content(schema = @Schema(implementation = AdminCreateUserRequest.class),
              examples = @ExampleObject(name = "Create user", value = """
                  {
                    "username": "newuser",
                    "password": "securePass123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "roles": ["ROLE_STAFF"],
                    "organizationId": 1
                  }
                  """)))
      @Valid @RequestBody AdminCreateUserRequest request
  ) {
    log.info("Admin creating user username={}", request.username());
    return ResponseEntity.ok(userService.createUser(request));
  }
}
