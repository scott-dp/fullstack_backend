package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import stud.ntnu.no.fullstack_project.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

  private final AuthService authService;

  @Value("${app.security.cookie-name}")
  private String cookieName;

  @PostMapping("/register")
  @Operation(summary = "Register a new user")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody AuthRequest request,
      HttpServletResponse response
  ) {
    return ResponseEntity.ok(authService.register(request, response));
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate a user")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody AuthRequest request,
      HttpServletResponse response
  ) {
    return ResponseEntity.ok(authService.authenticate(request, response));
  }

  @PostMapping("/logout")
  @Operation(summary = "Clear the auth cookie")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    authService.logout(response);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/status")
  @Operation(summary = "Get current authentication status")
  public ResponseEntity<AuthStatusResponse> status(HttpServletRequest request) {
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
