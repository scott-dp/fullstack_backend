package stud.ntnu.no.fullstack_project.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
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

  public AuthResponse register(AuthRequest request, HttpServletResponse response) {
    if (appUserRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username is already taken");
    }

    AppUser user = new AppUser();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.getRoles().add(Role.ROLE_STAFF);
    appUserRepository.save(user);

    return authenticate(request, response);
  }

  public AuthResponse authenticate(AuthRequest request, HttpServletResponse response) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );

    AppUser user = appUserRepository.findByUsername(request.username())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String token = jwtService.generateToken(user);
    addCookie(response, token, accessTokenExpiration);

    CurrentUserResponse currentUser = userService.mapToResponse(user);
    return new AuthResponse("Authentication successful", currentUser);
  }

  public AuthStatusResponse getAuthStatus(String token) {
    if (token == null || token.isBlank() || !jwtService.isTokenValid(token)) {
      return new AuthStatusResponse(false, null);
    }

    AppUser user = appUserRepository.findByUsername(jwtService.extractUsername(token))
        .orElse(null);

    if (user == null) {
      return new AuthStatusResponse(false, null);
    }

    return new AuthStatusResponse(true, userService.mapToResponse(user));
  }

  public void logout(HttpServletResponse response) {
    addCookie(response, "", 0);
  }

  private void addCookie(HttpServletResponse response, String token, long maxAgeMillis) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, token)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite(cookieSecure ? "None" : "Lax")
        .path("/")
        .maxAge(Duration.ofMillis(maxAgeMillis))
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
