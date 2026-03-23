package stud.ntnu.no.fullstack_project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import stud.ntnu.no.fullstack_project.service.AppUserDetailsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AppUserDetailsService userDetailsService;

  @Value("${app.security.cookie-name}")
  private String cookieName;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String requestUri = request.getRequestURI();

    if (requestUri.startsWith("/api/auth")
        || requestUri.startsWith("/api/health")
        || requestUri.startsWith("/swagger-ui")
        || requestUri.startsWith("/v3/api-docs")
        || requestUri.startsWith("/h2-console")) {
      log.trace("Skipping JWT filter for requestUri={}", requestUri);
      filterChain.doFilter(request, response);
      return;
    }

    String token = extractToken(request.getCookies());

    if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
      log.trace("JWT filter continuing without authentication tokenPresent={} authenticationAlreadySet={}",
          token != null,
          SecurityContextHolder.getContext().getAuthentication() != null);
      filterChain.doFilter(request, response);
      return;
    }

    String username;

    try {
      username = jwtService.extractUsername(token);
    } catch (RuntimeException exception) {
      log.warn("JWT extraction failed for requestUri={} error={}", requestUri, exception.getMessage());
      filterChain.doFilter(request, response);
      return;
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (jwtService.isTokenValid(token, userDetails)) {
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      log.debug("JWT authentication established username={} authorities={}",
          username,
          userDetails.getAuthorities());
    } else {
      log.warn("JWT token validation failed username={} requestUri={}", username, requestUri);
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(Cookie[] cookies) {
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
