package stud.ntnu.no.fullstack_project.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void hasThreeRoles() {
    assertEquals(3, Role.values().length);
  }

  @Test
  void containsExpectedRoles() {
    assertNotNull(Role.valueOf("ROLE_ADMIN"));
    assertNotNull(Role.valueOf("ROLE_MANAGER"));
    assertNotNull(Role.valueOf("ROLE_STAFF"));
  }

  @Test
  void valueOfInvalidRole_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> Role.valueOf("ROLE_SUPERADMIN"));
  }

  @Test
  void ordinalValues_areCorrect() {
    assertEquals(0, Role.ROLE_ADMIN.ordinal());
    assertEquals(1, Role.ROLE_MANAGER.ordinal());
    assertEquals(2, Role.ROLE_STAFF.ordinal());
  }
}
