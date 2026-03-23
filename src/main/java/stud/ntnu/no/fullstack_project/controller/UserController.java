package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for the authenticated user")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "Get the currently authenticated user")
  public ResponseEntity<CurrentUserResponse> getCurrentUser() {
    return ResponseEntity.ok(userService.getCurrentUser());
  }
}
