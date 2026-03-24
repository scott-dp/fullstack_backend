package stud.ntnu.no.fullstack_project.entity;

/**
 * User roles for access control.
 * ROLE_ADMIN: Full system access, can manage organizations and users.
 * ROLE_MANAGER: Can manage checklists, view reports, assign tasks within their organization.
 * ROLE_STAFF: Can complete checklists, log temperatures, and report deviations.
 */
public enum Role {
  ROLE_ADMIN,
  ROLE_MANAGER,
  ROLE_STAFF
}
