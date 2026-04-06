package stud.ntnu.no.fullstack_project.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.config.JwtService;
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
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.auth.Role;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.service.admin.UserService;

/**
 * Service responsible for user authentication operations.
 *
 * <p>Handles registration, login, logout, and authentication status checks
 * using JWT tokens stored in HTTP-only cookies.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private static final Random RANDOM = new Random();

  private final AppUserRepository appUserRepository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserService userService;
  private final VerificationEmailService verificationEmailService;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${app.security.cookie-name}")
  private String cookieName;

  @Value("${app.security.cookie-secure}")
  private boolean cookieSecure;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.auth.email-code-expiration-minutes:10}")
  private long emailCodeExpirationMinutes;

  /**
   * Registers a new user and immediately authenticates that user.
   *
   * @param request  incoming credential payload
   * @return authentication response for the newly registered user
   */
  @Transactional
  public RegistrationResponse register(RegisterRequest request) {
    String normalizedEmail = normalizeEmail(request.email());
    log.info("Attempting to register user username={} email={}", request.username(), normalizedEmail);
    if (appUserRepository.existsByUsername(request.username())) {
      log.warn("Registration rejected because username is already taken username={}", request.username());
      throw new IllegalArgumentException("Username is already taken");
    }
    if (appUserRepository.existsByEmail(normalizedEmail)) {
      log.warn("Registration rejected because email is already in use email={}", normalizedEmail);
      throw new IllegalArgumentException("Email is already in use");
    }

    AppUser user = new AppUser();
    user.setUsername(request.username());
    user.setEmail(normalizedEmail);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.getRoles().add(Role.ROLE_STAFF);
    user.setEmailVerified(false);
    user.setEmailVerificationToken(UUID.randomUUID().toString());
    user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(24));
    appUserRepository.save(user);
    String verificationLink = buildVerificationLink(user.getEmailVerificationToken());
    verificationEmailService.sendVerificationEmail(user.getEmail(), verificationLink);
    log.info("User registered successfully username={} email={} verificationLink={}",
        user.getUsername(), user.getEmail(), verificationLink);

    return new RegistrationResponse(
        "Registration successful. Check your email to verify your account before logging in."
    );
  }

  /**
   * Authenticates a user with the supplied credentials and sets a JWT cookie.
   *
   * @param request  incoming credential payload
   * @param response HTTP response used to write the auth cookie
   * @return authentication response containing the user profile
   */
  public AuthResponse authenticate(LoginRequest request, HttpServletResponse response) {
    AppUser account = appUserRepository.findByUsernameOrEmail(request.identifier(), normalizeEmail(request.identifier()))
        .orElseThrow(() -> new BadCredentialsException("Invalid username, email, or password"));

    if (!account.isEnabled() && account.getAccountSetupToken() != null) {
      throw new IllegalArgumentException("Complete your account setup from the email invitation before logging in");
    }
    if (!account.isEmailVerified()) {
      throw new IllegalArgumentException("Verify your email before logging in");
    }

    log.info("Attempting to authenticate user username={} identifier={}", account.getUsername(), request.identifier());
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(account.getUsername(), request.password())
    );

    AppUser user = appUserRepository.findByUsername(account.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String token = jwtService.generateToken(user);
    addCookie(response, token, accessTokenExpiration);
    log.info("Authentication successful username={} roles={}", user.getUsername(), user.getRoles());

    CurrentUserResponse currentUser = userService.mapToResponse(user);
    return new AuthResponse("Authentication successful", currentUser);
  }

  /**
   * Emails a one-time login code to a verified account.
   *
   * @param request verified email request
   * @return status message
   */
  @Transactional
  public MessageResponse requestEmailLoginCode(EmailCodeRequest request) {
    AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
        .orElseThrow(() -> new IllegalArgumentException("No user found for that email"));

    if (!user.isEnabled() && user.getAccountSetupToken() != null) {
      throw new IllegalArgumentException("Complete your account setup before requesting a login code");
    }
    if (!user.isEmailVerified()) {
      throw new IllegalArgumentException("Verify your email before requesting a login code");
    }

    String code = generateEmailLoginCode();
    user.setEmailLoginCode(code);
    user.setEmailLoginCodeExpiresAt(LocalDateTime.now().plusMinutes(emailCodeExpirationMinutes));
    appUserRepository.save(user);
    verificationEmailService.sendLoginCodeEmail(user.getEmail(), code);
    log.info("Issued email login code for user username={} email={}", user.getUsername(), user.getEmail());
    return new MessageResponse("A login code has been sent to your email.");
  }

  /**
   * Authenticates a user with a verified email address and one-time code.
   *
   * @param request email code login request
   * @param response HTTP response used to write the auth cookie
   * @return authentication response containing the user profile
   */
  @Transactional
  public AuthResponse authenticateWithEmailCode(
      EmailCodeLoginRequest request,
      HttpServletResponse response
  ) {
    AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
        .orElseThrow(() -> new BadCredentialsException("Invalid email or login code"));

    if (!user.isEnabled() && user.getAccountSetupToken() != null) {
      throw new IllegalArgumentException("Complete your account setup before logging in");
    }
    if (!user.isEmailVerified()) {
      throw new IllegalArgumentException("Verify your email before logging in");
    }
    if (user.getEmailLoginCode() == null || user.getEmailLoginCodeExpiresAt() == null) {
      throw new BadCredentialsException("Invalid email or login code");
    }
    if (user.getEmailLoginCodeExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Login code has expired");
    }
    if (!user.getEmailLoginCode().equals(request.code())) {
      throw new BadCredentialsException("Invalid email or login code");
    }

    user.setEmailLoginCode(null);
    user.setEmailLoginCodeExpiresAt(null);
    appUserRepository.save(user);

    String token = jwtService.generateToken(user);
    addCookie(response, token, accessTokenExpiration);
    log.info("Email code authentication successful username={} email={}", user.getUsername(), user.getEmail());
    return new AuthResponse("Authentication successful", userService.mapToResponse(user));
  }

  /**
   * Verifies a registration token and activates the corresponding account.
   *
   * @param token the verification token from the email link
   * @return verification status response
   */
  @Transactional
  public VerificationResponse verifyEmail(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Verification token is required");
    }

    AppUser user = appUserRepository.findByEmailVerificationToken(token.trim())
        .orElseThrow(() -> new IllegalArgumentException("Verification token was not found"));

    if (user.isEmailVerified()) {
      return new VerificationResponse("Email is already verified. You can log in.");
    }

    if (user.getEmailVerificationExpiresAt() == null
        || user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Verification token has expired");
    }

    user.setEmailVerified(true);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationExpiresAt(null);
    appUserRepository.save(user);
    log.info("Email verified for user username={} email={}", user.getUsername(), user.getEmail());
    return new VerificationResponse("Email verified successfully. You can now log in.");
  }
  /**
   * Resolves the invited admin account referenced by a setup token and returns
   * the prefilled information needed by the setup page.
   *
   * @param token one-time admin setup token
   * @return setup information for the invited admin account
   */
  public AdminSetupInfoResponse getAdminSetupInfo(String token) {
    AppUser user = findPendingAdminSetup(token);
    return new AdminSetupInfoResponse(
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getOrganization() != null ? user.getOrganization().getName() : null
    );
  }

  /**
   * Finalizes the first-time setup flow for an invited admin account.
   *
   * <p>The method validates the setup token, stores the chosen password,
   * enables the account, marks the email as verified, and clears any setup
   * or verification tokens that should no longer be reusable.</p>
   *
   * @param request token and password submitted from the setup form
   * @return success message for the completed setup flow
   */
  @Transactional
  public MessageResponse completeAdminSetup(CompleteAdminSetupRequest request) {
    AppUser user = findPendingAdminSetup(request.token());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setEnabled(true);
    user.setEmailVerified(true);
    user.setAccountSetupToken(null);
    user.setAccountSetupExpiresAt(null);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationExpiresAt(null);
    appUserRepository.save(user);
    log.info("Completed invited admin account setup for username={}", user.getUsername());
    return new MessageResponse("Account setup complete. You can now log in.");
  }

  /**
   * Returns the current authentication status based on the provided JWT token.
   *
   * @param token the JWT token extracted from the request cookies, or {@code null}
   * @return authentication status with user profile if authenticated
   */
  public AuthStatusResponse getAuthStatus(String token) {
    log.debug("Checking authentication status tokenPresent={}", token != null && !token.isBlank());
    if (token == null || token.isBlank() || !jwtService.isTokenValid(token)) {
      log.debug("Authentication status resolved to anonymous");
      return new AuthStatusResponse(false, null);
    }

    String username = jwtService.extractUsername(token);
    AppUser user = appUserRepository.findByUsername(username)
        .orElse(null);

    if (user == null) {
      log.warn("Authentication token was valid but user was not found username={}", username);
      return new AuthStatusResponse(false, null);
    }

    log.debug("Authentication status resolved to authenticated username={}", user.getUsername());
    return new AuthStatusResponse(true, userService.mapToResponse(user));
  }

  /**
   * Logs the user out by clearing the authentication cookie.
   *
   * @param response HTTP response used to clear the auth cookie
   */
  public void logout(HttpServletResponse response) {
    log.info("Logging out user");
    addCookie(response, "", 0);
  }

  /**
   * Writes a JWT cookie to the HTTP response.
   *
   * @param response     HTTP response to add the cookie to
   * @param token        the JWT token value
   * @param maxAgeMillis maximum age of the cookie in milliseconds
   */
  private void addCookie(HttpServletResponse response, String token, long maxAgeMillis) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, token)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite(cookieSecure ? "None" : "Lax")
        .path("/")
        .maxAge(Duration.ofMillis(maxAgeMillis))
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    log.debug("Auth cookie updated cookieName={} secure={} maxAgeMillis={}", cookieName, cookieSecure, maxAgeMillis);
  }

  /**
   * Normalizes an email address for repository lookups and uniqueness checks.
   *
   * @param email raw email input
   * @return trimmed, lowercase email value
   */
  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Builds the frontend verification URL for a registration token.
   *
   * @param token email verification token
   * @return absolute frontend verification link
   */
  private String buildVerificationLink(String token) {
    return frontendUrl + "/verify-email?token=" + token;
  }

  /**
   * Finds a still-pending invited admin account by setup token.
   *
   * <p>The token must exist, must not be expired, must still belong to a
   * disabled account, and must be tied to an admin role.</p>
   *
   * @param token one-time admin setup token
   * @return invited admin account awaiting completion
   */
  private AppUser findPendingAdminSetup(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Setup token is required");
    }

    AppUser user = appUserRepository.findByAccountSetupToken(token.trim())
        .orElseThrow(() -> new IllegalArgumentException("Setup token was not found"));

    if (user.getAccountSetupExpiresAt() == null || user.getAccountSetupExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Setup token has expired");
    }
    if (user.isEnabled() || user.getAccountSetupToken() == null) {
      throw new IllegalArgumentException("Account setup has already been completed");
    }
    if (!user.getRoles().contains(Role.ROLE_ADMIN)) {
      throw new IllegalArgumentException("Setup token is not valid for an admin account");
    }

    return user;
  }

  /**
   * Generates a six-digit one-time login code for email-based sign-in.
   *
   * @return zero-padded six-digit login code
   */
  private String generateEmailLoginCode() {
    return String.format("%06d", RANDOM.nextInt(1_000_000));
  }
}
