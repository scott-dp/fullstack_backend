package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.UserService;

/**
 * REST controller for authenticated user operations.
 *
 * <p>This controller currently exposes a single endpoint for retrieving the
 * principal resolved by the authentication flow.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints for the authenticated user")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(
      summary = "Get the currently authenticated user",
      description = "Returns the currently authenticated user based on the JWT cookie "
          + "resolved by Spring Security."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Current user returned successfully",
          content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Request is not authenticated",
          content = @Content(schema = @Schema(implementation = ApiError.class))
      )
  })
  public ResponseEntity<CurrentUserResponse> getCurrentUser() {
    log.info("Received request for current authenticated user");
    return ResponseEntity.ok(userService.getCurrentUser());
  }
}
