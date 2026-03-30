package stud.ntnu.no.fullstack_project.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for role parsing and role helper behavior.
 */
import stud.ntnu.no.fullstack_project.entity.auth.Role;

class RoleTest {

  @Test
  void hasFourRoles() {
    assertEquals(4, Role.values().length);
  }

  @Test
  void containsExpectedRoles() {
    assertNotNull(Role.valueOf("ROLE_SUPERADMIN"));
    assertNotNull(Role.valueOf("ROLE_ADMIN"));
    assertNotNull(Role.valueOf("ROLE_MANAGER"));
    assertNotNull(Role.valueOf("ROLE_STAFF"));
  }

  @Test
  void valueOfInvalidRole_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> Role.valueOf("ROLE_OWNER"));
  }

  @Test
  void ordinalValues_areCorrect() {
    assertEquals(0, Role.ROLE_SUPERADMIN.ordinal());
    assertEquals(1, Role.ROLE_ADMIN.ordinal());
    assertEquals(2, Role.ROLE_MANAGER.ordinal());
    assertEquals(3, Role.ROLE_STAFF.ordinal());
  }
}
