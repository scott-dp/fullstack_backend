package stud.ntnu.no.fullstack_project.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .claims(Map.of("roles", userDetails.getAuthorities().stream().map(Object::toString).toList()))
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      Claims claims = extractClaims(token);
      return claims.getSubject().equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }

  public boolean isTokenValid(String token) {
    try {
      return extractClaims(token).getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
}
