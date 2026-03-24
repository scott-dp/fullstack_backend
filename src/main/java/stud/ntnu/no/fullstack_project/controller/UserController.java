package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import stud.ntnu.no.fullstack_project.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "Get the currently authenticated user")
  public ResponseEntity<CurrentUserResponse> getCurrentUser() {
    return ResponseEntity.ok(userService.getCurrentUser());
  }

  @PutMapping("/me")
  @Operation(summary = "Update the currently authenticated user's profile")
  public ResponseEntity<CurrentUserResponse> updateProfile(
      @Valid @RequestBody UpdateUserRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(userService.updateProfile(request, currentUser));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "List users in the current user's organization")
  public ResponseEntity<List<UserSummaryResponse>> listUsers(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(
        userService.listUsersInOrganization(currentUser.getOrganization().getId())
    );
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Admin creates a new user")
  public ResponseEntity<CurrentUserResponse> createUser(
      @Valid @RequestBody AdminCreateUserRequest request
  ) {
    return ResponseEntity.ok(userService.createUser(request));
  }
}
