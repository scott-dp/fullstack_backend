package stud.ntnu.no.fullstack_project.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.superadmin.CreateOrganizationAdminRequest;
import stud.ntnu.no.fullstack_project.dto.superadmin.OrganizationAdminSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.auth.Role;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;
import stud.ntnu.no.fullstack_project.service.auth.VerificationEmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminService {

  private final AppUserRepository appUserRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final VerificationEmailService verificationEmailService;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  /**
   * Creates a new organization together with its first invited admin account.
   *
   * <p>The organization is created first, then a disabled admin user is stored
   * with a one-time setup token and invited by email.</p>
   *
   * @param request organization and admin details from the superadmin UI
   * @return summary of the invited admin account
   */
  @Transactional
  public OrganizationAdminSummaryResponse createOrganizationWithAdmin(CreateOrganizationAdminRequest request) {
    String normalizedEmail = normalizeEmail(request.email());
    String normalizedOrgNumber = normalizeOptional(request.organizationNumber());

    if (appUserRepository.existsByEmail(normalizedEmail)) {
      throw new IllegalArgumentException("Email is already in use");
    }
    if (normalizedOrgNumber != null && organizationRepository.existsByOrganizationNumber(normalizedOrgNumber)) {
      throw new IllegalArgumentException("Organization number is already registered");
    }

    OrganizationType type = parseOrganizationType(request.organizationType());

    Organization organization = new Organization();
    organization.setName(request.organizationName().trim());
    organization.setOrganizationNumber(normalizedOrgNumber);
    organization.setType(type);
    organization = organizationRepository.save(organization);

    AppUser admin = new AppUser();
    admin.setUsername(generateUniqueUsername(normalizedEmail));
    admin.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    admin.setFirstName(request.firstName().trim());
    admin.setLastName(normalizeOptional(request.lastName()));
    admin.setEmail(normalizedEmail);
    admin.setEmailVerified(false);
    admin.setEnabled(false);
    admin.setOrganization(organization);
    admin.setRoles(Set.of(Role.ROLE_ADMIN));
    admin.setAccountSetupToken(UUID.randomUUID().toString());
    admin.setAccountSetupExpiresAt(LocalDateTime.now().plusDays(7));
    admin = appUserRepository.save(admin);

    verificationEmailService.sendAdminSetupEmail(
        admin.getEmail(),
        buildAdminSetupLink(admin.getAccountSetupToken()),
        organization.getName()
    );

    log.info("Superadmin created organization id={} and invited admin id={}", organization.getId(), admin.getId());
    return mapAdminSummary(admin);
  }

  /**
   * Lists all organization-scoped admin accounts across the platform.
   *
   * @return summaries of all organization admins
   */
  public List<OrganizationAdminSummaryResponse> listOrganizationAdmins() {
    return appUserRepository.findAllByRole(Role.ROLE_ADMIN).stream()
        .map(this::mapAdminSummary)
        .toList();
  }

  /**
   * Archives an organization admin account from the superadmin area.
   *
   * <p>This disables the target account and clears active setup or login-code
   * state, but does not hard-delete the user record.</p>
   *
   * @param userId target admin identifier
   * @param currentUser authenticated superadmin performing the action
   */
  @Transactional
  public void archiveOrganizationAdmin(Long userId, AppUser currentUser) {
    AppUser target = appUserRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (target.getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("You cannot archive your own account");
    }
    if (!target.getRoles().contains(Role.ROLE_ADMIN)) {
      throw new IllegalArgumentException("Only organization admin accounts can be archived here");
    }
    if (target.getRoles().contains(Role.ROLE_SUPERADMIN)) {
      throw new IllegalArgumentException("Superadmin accounts cannot be archived here");
    }

    target.setEnabled(false);
    target.setEmailLoginCode(null);
    target.setEmailLoginCodeExpiresAt(null);
    target.setAccountSetupToken(null);
    target.setAccountSetupExpiresAt(null);
    appUserRepository.save(target);

    log.info("Superadmin archived organization admin id={}", userId);
  }

  /**
   * Derives a unique username candidate from an email address.
   *
   * <p>The local part of the email is sanitized and then suffixed with an
   * incrementing counter until an unused username is found.</p>
   *
   * @param email normalized admin email address
   * @return unique username for the invited admin
   */
  private String generateUniqueUsername(String email) {
    String localPart = email.substring(0, email.indexOf('@'));
    String base = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
    if (base.isBlank()) {
      base = "admin";
    }

    String candidate = base;
    int counter = 2;
    while (appUserRepository.existsByUsername(candidate)) {
      candidate = base + counter;
      counter++;
    }
    return candidate;
  }

  /**
   * Parses the requested organization type string into the matching enum value.
   *
   * @param value raw organization type from the request
   * @return parsed organization type
   */
  private OrganizationType parseOrganizationType(String value) {
    try {
      return OrganizationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Invalid organization type: " + value);
    }
  }

  /**
   * Normalizes an email address for uniqueness checks and persistence.
   *
   * @param email raw email input
   * @return trimmed, lowercase email value
   */
  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Normalizes an optional text field by trimming whitespace and converting
   * blank values to {@code null}.
   *
   * @param value optional raw text value
   * @return trimmed text or {@code null} if blank or missing
   */
  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  /**
   * Builds the one-time frontend link used by an invited admin to finish
   * account setup.
   *
   * @param token account setup token
   * @return absolute frontend setup URL
   */
  private String buildAdminSetupLink(String token) {
    return frontendUrl + "/admin-setup?token=" + token;
  }

  /**
   * Maps an admin user entity to the lightweight summary shown in the
   * superadmin dashboard.
   *
   * @param user organization admin account
   * @return summary response for list and create operations
   */
  private OrganizationAdminSummaryResponse mapAdminSummary(AppUser user) {
    Organization organization = user.getOrganization();
    return new OrganizationAdminSummaryResponse(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        organization != null ? organization.getId() : null,
        organization != null ? organization.getName() : null,
        user.isEnabled(),
        user.getAccountSetupToken() != null,
        user.getCreatedAt()
    );
  }
}
