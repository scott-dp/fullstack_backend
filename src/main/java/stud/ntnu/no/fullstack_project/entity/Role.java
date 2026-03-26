package stud.ntnu.no.fullstack_project.entity;

/**
 * User roles for access control.
 * ROLE_SUPERADMIN: Global system access across organizations.
 * ROLE_ADMIN: Organization-level administrator.
 * ROLE_MANAGER: Can manage checklists, view reports, assign tasks within their organization.
 * ROLE_STAFF: Can complete checklists, log temperatures, and report deviations.
 */
public enum Role {
  ROLE_SUPERADMIN,
  ROLE_ADMIN,
  ROLE_MANAGER,
  ROLE_STAFF
}
