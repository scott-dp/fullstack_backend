package stud.ntnu.no.fullstack_project.controller.auth;

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
import stud.ntnu.no.fullstack_project.dto.auth.AuthResponse;
import stud.ntnu.no.fullstack_project.dto.auth.AdminSetupInfoResponse;
import stud.ntnu.no.fullstack_project.dto.auth.AuthStatusResponse;
import stud.ntnu.no.fullstack_project.dto.auth.CompleteAdminSetupRequest;
import stud.ntnu.no.fullstack_project.dto.auth.EmailCodeLoginRequest;
import stud.ntnu.no.fullstack_project.dto.auth.EmailCodeRequest;
import stud.ntnu.no.fullstack_project.dto.auth.LoginRequest;
import stud.ntnu.no.fullstack_project.dto.auth.MessageResponse;
import stud.ntnu.no.fullstack_project.dto.auth.RegisterRequest;
import stud.ntnu.no.fullstack_project.dto.auth.RegistrationResponse;
import stud.ntnu.no.fullstack_project.dto.auth.VerificationResponse;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.auth.AuthService;

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
      @ApiResponse(responseCode = "200", description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or username is already taken",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<RegistrationResponse> register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Credentials for the new user account.",
          required = true,
          content = @Content(schema = @Schema(implementation = RegisterRequest.class),
              examples = @ExampleObject(name = "Register request", value = """
                  {"username": "scott", "email": "scott@example.com", "password": "superSecret123"}
                  """)))
      @Valid @RequestBody RegisterRequest request
  ) {
    log.info("Attempting to register user username={}", request.username());
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate a user",
      description = "Validates the supplied credentials and, on success, sets an HTTP-only JWT "
          + "cookie on the response."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authenticated successfully",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid username or password",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AuthResponse> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Credentials for authentication.",
          required = true,
          content = @Content(schema = @Schema(implementation = LoginRequest.class),
              examples = @ExampleObject(name = "Login request", value = """
                  {"identifier": "scott@example.com", "password": "superSecret123"}
                  """)))
      @Valid @RequestBody LoginRequest request,
      HttpServletResponse response
  ) {
    log.info("Attempting to authenticate identifier={}", request.identifier());
    return ResponseEntity.ok(authService.authenticate(request, response));
  }

  @PostMapping("/email-code/request")
  @Operation(
      summary = "Send a one-time login code to email",
      description = "Sends a one-time code to a verified email address for passwordless sign-in."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login code sent successfully",
          content = @Content(schema = @Schema(implementation = MessageResponse.class))),
      @ApiResponse(responseCode = "400", description = "Email not found or not verified",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<MessageResponse> requestEmailCode(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Verified email address for code delivery.",
          required = true,
          content = @Content(schema = @Schema(implementation = EmailCodeRequest.class)))
      @Valid @RequestBody EmailCodeRequest request
  ) {
    log.info("Email login code requested");
    return ResponseEntity.ok(authService.requestEmailLoginCode(request));
  }

  @PostMapping("/email-code/login")
  @Operation(
      summary = "Authenticate with email and one-time code",
      description = "Signs the user in using a previously emailed one-time login code."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authenticated successfully",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "400", description = "Code expired or email not verified",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Invalid email or login code",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AuthResponse> loginWithEmailCode(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Email address and one-time code.",
          required = true,
          content = @Content(schema = @Schema(implementation = EmailCodeLoginRequest.class)))
      @Valid @RequestBody EmailCodeLoginRequest request,
      HttpServletResponse response
  ) {
    log.info("Email code login requested for email={}", request.email());
    return ResponseEntity.ok(authService.authenticateWithEmailCode(request, response));
  }

  @GetMapping("/verify")
  @Operation(
      summary = "Verify a newly registered email address",
      description = "Activates an account using the email verification token returned at registration time."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Email verified successfully",
          content = @Content(schema = @Schema(implementation = VerificationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid or expired token",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<VerificationResponse> verifyEmail(
      @org.springframework.web.bind.annotation.RequestParam String token
  ) {
    log.info("Email verification requested");
    return ResponseEntity.ok(authService.verifyEmail(token));
  }

  @GetMapping("/admin-setup")
  @Operation(
      summary = "Inspect a pending admin setup token",
      description = "Returns the basic account details for a valid one-time organization admin setup token."
  )
  public ResponseEntity<AdminSetupInfoResponse> getAdminSetupInfo(
      @org.springframework.web.bind.annotation.RequestParam String token
  ) {
    log.info("Admin setup info requested");
    return ResponseEntity.ok(authService.getAdminSetupInfo(token));
  }

  @PostMapping("/admin-setup")
  @Operation(
      summary = "Complete invited admin account setup",
      description = "Sets the password for an invited organization admin and activates the account."
  )
  public ResponseEntity<MessageResponse> completeAdminSetup(
      @Valid @RequestBody CompleteAdminSetupRequest request
  ) {
    log.info("Admin setup completion requested");
    return ResponseEntity.ok(authService.completeAdminSetup(request));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Clear the auth cookie",
      description = "Clears the JWT authentication cookie, effectively logging the user out."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Logout successful")
  })
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    log.info("User logout requested");
    authService.logout(response);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/status")
  @Operation(
      summary = "Get current authentication status",
      description = "Returns whether the caller is currently authenticated and, if so, the "
          + "current user's profile."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authentication status returned",
          content = @Content(schema = @Schema(implementation = AuthStatusResponse.class)))
  })
  public ResponseEntity<AuthStatusResponse> status(HttpServletRequest request) {
    log.info("Auth status check requested");
    return ResponseEntity.ok(authService.getAuthStatus(extractToken(request)));
  }

  /**
   * Extracts the JWT token from the request cookies.
   *
   * @param request the incoming HTTP request
   * @return the token value, or {@code null} if not present
   */
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
