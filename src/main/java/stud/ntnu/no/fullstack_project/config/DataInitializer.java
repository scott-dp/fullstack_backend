package stud.ntnu.no.fullstack_project.config;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

/**
 * Seeds the database with a default organization and sample users on first run.
 *
 * <p>Only executes when the user table is empty, ensuring idempotent behaviour.
 * Creates one organization and three users (admin, manager, staff) with
 * pre-configured credentials for development and testing purposes.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final AppUserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Runs the seed logic on application startup if no users exist.
   *
   * @param args command-line arguments (ignored)
   */
  @Override
  public void run(String... args) {
    if (userRepository.count() > 0) {
      return;
    }

    log.info("Initializing seed data...");

    Organization org = new Organization();
    org.setName("Everest Sushi & Fusion");
    org.setOrganizationNumber("937219997");
    org.setAddress("Trondheim, Norway");
    org.setPhone("+47 123 45 678");
    org.setType(OrganizationType.RESTAURANT);
    org = organizationRepository.save(org);

    AppUser admin = new AppUser();
    admin.setUsername("admin");
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFirstName("System");
    admin.setLastName("Administrator");
    admin.setEmail("admin@everest.no");
    admin.setEmailVerified(true);
    admin.setOrganization(org);
    admin.setRoles(Set.of(Role.ROLE_ADMIN));
    userRepository.save(admin);

    AppUser manager = new AppUser();
    manager.setUsername("manager");
    manager.setPassword(passwordEncoder.encode("manager123"));
    manager.setFirstName("Restaurant");
    manager.setLastName("Manager");
    manager.setEmail("manager@everest.no");
    manager.setEmailVerified(true);
    manager.setOrganization(org);
    manager.setRoles(Set.of(Role.ROLE_MANAGER));
    userRepository.save(manager);

    AppUser staff = new AppUser();
    staff.setUsername("staff");
    staff.setPassword(passwordEncoder.encode("staff123"));
    staff.setFirstName("Kitchen");
    staff.setLastName("Staff");
    staff.setEmail("staff@everest.no");
    staff.setEmailVerified(true);
    staff.setOrganization(org);
    staff.setRoles(Set.of(Role.ROLE_STAFF));
    userRepository.save(staff);

    log.info("Seed data initialized: 1 organization, 3 users (admin/manager/staff)");
  }
}
