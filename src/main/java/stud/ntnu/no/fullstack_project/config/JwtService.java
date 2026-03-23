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

@Service
@Slf4j
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  /**
   * Generates a JWT token for the supplied authenticated user.
   *
   * @param userDetails authenticated user details
   * @return signed JWT token string
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
   * Extracts the username claim from the supplied JWT token.
   *
   * @param token signed JWT token
   * @return username stored in the token subject claim
   */
  public String extractUsername(String token) {
    String username = extractClaims(token).getSubject();
    log.trace("Extracted username from JWT username={}", username);
    return username;
  }

  /**
   * Validates a JWT token against a specific user.
   *
   * @param token signed JWT token
   * @param userDetails expected authenticated user
   * @return {@code true} if the token belongs to the user and is not expired
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
   * Validates a JWT token independently of a specific user lookup.
   *
   * @param token signed JWT token
   * @return {@code true} if the token is parseable and unexpired
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
   * Parses all claims from the supplied signed JWT token.
   *
   * @param token signed JWT token
   * @return parsed claims payload
   */
  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Creates the HMAC signing key used for token signing and validation.
   *
   * @return shared secret signing key
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
}
