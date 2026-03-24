package stud.ntnu.no.fullstack_project.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Utility service for creating and validating JSON Web Tokens (JWT).
 *
 * <p>Tokens are signed with an HMAC-SHA key derived from the configured secret.
 * Each token contains the username as its subject and the user's roles as a
 * custom claim.</p>
 */
@Service
@Slf4j
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  /**
   * Generates a signed JWT for the given user.
   *
   * @param userDetails the authenticated user whose token is being generated
   * @return a compact, signed JWT string
   */
  public String generateToken(UserDetails userDetails) {
    log.debug("Generating JWT token username={} authorities={}",
        userDetails.getUsername(),
        userDetails.getAuthorities());
    return Jwts.builder()
        .claims(Map.of("roles", userDetails.getAuthorities().stream().map(Object::toString).toList()))
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extracts the username (subject) from a JWT.
   *
   * @param token the JWT string
   * @return the username stored in the token's subject claim
   */
  public String extractUsername(String token) {
    String username = extractClaims(token).getSubject();
    log.trace("Extracted username from JWT username={}", username);
    return username;
  }

  /**
   * Validates a token against the given user details.
   *
   * @param token       the JWT string
   * @param userDetails the user to validate against
   * @return {@code true} if the token is valid and belongs to the user
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      Claims claims = extractClaims(token);
      return claims.getSubject().equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException exception) {
      log.debug("JWT validation failed for authenticated user lookup error={}", exception.getMessage());
      return false;
    }
  }

  /**
   * Validates a token without checking the user.
   *
   * @param token the JWT string
   * @return {@code true} if the token signature is valid and it has not expired
   */
  public boolean isTokenValid(String token) {
    try {
      return extractClaims(token).getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException exception) {
      log.debug("JWT validation failed error={}", exception.getMessage());
      return false;
    }
  }

  /**
   * Parses and verifies the claims from a JWT.
   *
   * @param token the JWT string
   * @return the parsed claims
   */
  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Derives the HMAC-SHA signing key from the configured secret.
   *
   * @return the secret key used for signing and verification
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
}
