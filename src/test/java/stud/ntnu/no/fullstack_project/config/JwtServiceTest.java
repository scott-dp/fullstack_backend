package stud.ntnu.no.fullstack_project.config;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Role;

class JwtServiceTest {

  private JwtService jwtService;

  private static final String SECRET_KEY = "this-is-a-very-secret-key-that-must-be-at-least-32-characters-long";
  private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour

  private AppUser testUser;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encoded");
    testUser.setRoles(new HashSet<>(Set.of(Role.ROLE_STAFF)));
  }

  // --- generateToken tests ---

  @Test
  void generateToken_returnsNonEmptyToken() {
    String token = jwtService.generateToken(testUser);

    assertNotNull(token);
    assertFalse(token.isBlank());
  }

  @Test
  void generateToken_tokenContainsThreeParts() {
    String token = jwtService.generateToken(testUser);

    // JWT has header.payload.signature
    String[] parts = token.split("\\.");
    assertEquals(3, parts.length);
  }

  // --- extractUsername tests ---

  @Test
  void extractUsername_returnsCorrectUsername() {
    String token = jwtService.generateToken(testUser);

    String username = jwtService.extractUsername(token);

    assertEquals("testuser", username);
  }

  @Test
  void extractUsername_differentUsers_returnDifferentUsernames() {
    AppUser adminUser = new AppUser();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setPassword("encoded");
    adminUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN)));

    String staffToken = jwtService.generateToken(testUser);
    String adminToken = jwtService.generateToken(adminUser);

    assertEquals("testuser", jwtService.extractUsername(staffToken));
    assertEquals("adminuser", jwtService.extractUsername(adminToken));
  }

  // --- isTokenValid (with UserDetails) tests ---

  @Test
  void isTokenValid_withUserDetails_returnsTrueForValidToken() {
    String token = jwtService.generateToken(testUser);

    assertTrue(jwtService.isTokenValid(token, testUser));
  }

  @Test
  void isTokenValid_withUserDetails_returnsFalseForWrongUser() {
    String token = jwtService.generateToken(testUser);

    AppUser otherUser = new AppUser();
    otherUser.setId(2L);
    otherUser.setUsername("otheruser");
    otherUser.setPassword("encoded");
    otherUser.setRoles(new HashSet<>(Set.of(Role.ROLE_STAFF)));

    assertFalse(jwtService.isTokenValid(token, otherUser));
  }

  @Test
  void isTokenValid_withUserDetails_returnsFalseForExpiredToken() {
    // Create a service with very short expiration
    JwtService shortLivedService = new JwtService();
    ReflectionTestUtils.setField(shortLivedService, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(shortLivedService, "accessTokenExpiration", 1L); // 1ms

    String token = shortLivedService.generateToken(testUser);

    // Wait briefly to ensure token expires
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertFalse(shortLivedService.isTokenValid(token, testUser));
  }

  // --- isTokenValid (without UserDetails) tests ---

  @Test
  void isTokenValid_noUserDetails_returnsTrueForValidToken() {
    String token = jwtService.generateToken(testUser);

    assertTrue(jwtService.isTokenValid(token));
  }

  @Test
  void isTokenValid_noUserDetails_returnsFalseForExpiredToken() {
    JwtService shortLivedService = new JwtService();
    ReflectionTestUtils.setField(shortLivedService, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(shortLivedService, "accessTokenExpiration", 1L);

    String token = shortLivedService.generateToken(testUser);

    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertFalse(shortLivedService.isTokenValid(token));
  }

  @Test
  void isTokenValid_returnsFalseForTamperedToken() {
    String token = jwtService.generateToken(testUser);

    // Tamper with the token payload
    String[] parts = token.split("\\.");
    String tamperedToken = parts[0] + "." + parts[1] + "a" + "." + parts[2];

    assertFalse(jwtService.isTokenValid(tamperedToken));
  }

  @Test
  void isTokenValid_returnsFalseForTokenSignedWithDifferentKey() {
    // Build a token manually with a different secret key
    String differentKey = "a-completely-different-secret-key-at-least-32-chars-long-xxx";
    SecretKey signingKey = Keys.hmacShaKeyFor(differentKey.getBytes(StandardCharsets.UTF_8));

    String token = Jwts.builder()
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(signingKey)
        .compact();

    assertFalse(jwtService.isTokenValid(token));
  }

  @Test
  void isTokenValid_returnsFalseForGarbageInput() {
    assertFalse(jwtService.isTokenValid("not.a.jwt"));
  }

  @Test
  void isTokenValid_returnsFalseForEmptyString() {
    assertFalse(jwtService.isTokenValid(""));
  }
}
