package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.auth.AuthRequest;
import stud.ntnu.no.fullstack_project.dto.auth.AuthResponse;
import stud.ntnu.no.fullstack_project.dto.auth.AuthStatusResponse;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.AuthService;

/**
 * REST controller for authentication-related endpoints.
 *
 * <p>Provides operations for registration, login, logout, and auth-status checks
 * using the cookie-based JWT flow configured in Spring Security.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

  private final AuthService authService;

  @Value("${app.security.cookie-name}")
  private String cookieName;

  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account, stores the encoded password, and authenticates "
          + "the user immediately by returning the auth cookie."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation failed or username is already taken",
          content = @Content(schema = @Schema(implementation = ApiError.class))
      )
  })
  public ResponseEntity<AuthResponse> register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Credentials for the new user account.",
          required = true,
          content = @Content(
              schema = @Schema(implementation = AuthRequest.class),
              examples = @ExampleObject(
                  name = "Register request",
                  value = """
                      {
                        "username": "scott",
                        "password": "superSecret123"
                      }
                      """
              )
          )
      )
      @Valid @RequestBody AuthRequest request,
      HttpServletResponse response
  ) {
    log.info("Received registration request for username={}", request.username());
    return ResponseEntity.ok(authService.register(request, response));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate a user",
      description = "Authenticates submitted credentials, generates a JWT, and writes it to the "
          + "configured HTTP-only cookie."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Authentication successful",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Invalid username or password",
          content = @Content(schema = @Schema(implementation = ApiError.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Malformed request body",
          content = @Content(schema = @Schema(implementation = ApiError.class))
      )
  })
  public ResponseEntity<AuthResponse> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Credentials used to log in.",
          required = true,
          content = @Content(
              schema = @Schema(implementation = AuthRequest.class),
              examples = @ExampleObject(
                  name = "Login request",
                  value = """
                      {
                        "username": "scott",
                        "password": "superSecret123"
                      }
                      """
              )
          )
      )
      @Valid @RequestBody AuthRequest request,
      HttpServletResponse response
  ) {
    log.info("Received login request for username={}", request.username());
    return ResponseEntity.ok(authService.authenticate(request, response));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Clear the auth cookie",
      description = "Clears the configured authentication cookie and effectively logs out the current client."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Logout completed successfully"),
  })
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    log.info("Received logout request");
    authService.logout(response);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/status")
  @Operation(
      summary = "Get current authentication status",
      description = "Checks the current auth cookie and returns whether the request is authenticated "
          + "together with the resolved user when available."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Authentication status resolved successfully",
          content = @Content(schema = @Schema(implementation = AuthStatusResponse.class))
      )
  })
  public ResponseEntity<AuthStatusResponse> status(HttpServletRequest request) {
    log.debug("Received auth status request");
    return ResponseEntity.ok(authService.getAuthStatus(extractToken(request)));
  }

  private String extractToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
