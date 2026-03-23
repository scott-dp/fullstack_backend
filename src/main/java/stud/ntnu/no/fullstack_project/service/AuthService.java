package stud.ntnu.no.fullstack_project.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.config.JwtService;
import stud.ntnu.no.fullstack_project.dto.auth.AuthRequest;
import stud.ntnu.no.fullstack_project.dto.auth.AuthResponse;
import stud.ntnu.no.fullstack_project.dto.auth.AuthStatusResponse;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final AppUserRepository appUserRepository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserService userService;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${app.security.cookie-name}")
  private String cookieName;

  @Value("${app.security.cookie-secure}")
  private boolean cookieSecure;

  /**
   * Registers a new user and immediately authenticates that user.
   *
   * @param request incoming credential payload
   * @param response HTTP response used to write the auth cookie
   * @return authentication response for the newly registered user
   */
  public AuthResponse register(AuthRequest request, HttpServletResponse response) {
    log.info("Attempting to register user username={}", request.username());

    if (appUserRepository.existsByUsername(request.username())) {
      log.warn("Registration rejected because username is already taken username={}", request.username());
      throw new IllegalArgumentException("Username is already taken");
    }

    AppUser user = new AppUser();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.getRoles().add(Role.ROLE_USER);
    appUserRepository.save(user);
    log.info("User registered successfully username={} roles={}", user.getUsername(), user.getRoles());

    return authenticate(request, response);
  }

  /**
   * Authenticates an existing user and writes the JWT cookie to the response.
   *
   * @param request incoming credential payload
   * @param response HTTP response used to write the auth cookie
   * @return authentication response for the authenticated user
   */
  public AuthResponse authenticate(AuthRequest request, HttpServletResponse response) {
    log.info("Authenticating user username={}", request.username());
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );

    AppUser user = appUserRepository.findByUsername(request.username())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String token = jwtService.generateToken(user);
    addCookie(response, token, accessTokenExpiration);
    log.info("Authentication successful username={} roles={}", user.getUsername(), user.getRoles());

    CurrentUserResponse currentUser = userService.mapToResponse(user);
    return new AuthResponse("Authentication successful", currentUser);
  }

  /**
   * Resolves whether the incoming request is authenticated based on the cookie token.
   *
   * @param token JWT token extracted from the configured auth cookie
   * @return authentication status payload for the current request context
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
   * Clears the configured auth cookie.
   *
   * @param response HTTP response used to write the clearing cookie
   */
  public void logout(HttpServletResponse response) {
    log.info("Clearing authentication cookie");
    addCookie(response, "", 0);
  }

  /**
   * Writes the auth cookie to the response.
   *
   * @param response HTTP response to modify
   * @param token token value to store in the cookie
   * @param maxAgeMillis cookie lifetime in milliseconds
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
}
