package stud.ntnu.no.fullstack_project.config;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.entity.TrainingCategory;
import stud.ntnu.no.fullstack_project.entity.TrainingTemplate;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingTemplateRepository;

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
  private final TrainingTemplateRepository trainingTemplateRepository;

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

    seedTrainingTemplates(org);

    log.info("Seed data initialized: 1 organization, 3 users (admin/manager/staff), training templates");
  }

  /**
   * Seeds default training templates for the given organization.
   *
   * @param org the organization to create training templates for
   */
  private void seedTrainingTemplates(Organization org) {
    TrainingTemplate foodHygiene = new TrainingTemplate();
    foodHygiene.setOrganization(org);
    foodHygiene.setTitle("Basic food hygiene");
    foodHygiene.setModuleType(ModuleType.IK_MAT);
    foodHygiene.setCategory(TrainingCategory.FOOD_HYGIENE);
    foodHygiene.setDescription("Covers basic food hygiene principles and safe food handling.");
    foodHygiene.setRequiredForRole(ResponsibleRole.ALL);
    foodHygiene.setMandatory(true);
    foodHygiene.setValidityDays(365);
    foodHygiene.setAcknowledgmentRequired(true);
    trainingTemplateRepository.save(foodHygiene);

    TrainingTemplate allergen = new TrainingTemplate();
    allergen.setOrganization(org);
    allergen.setTitle("Allergen awareness");
    allergen.setModuleType(ModuleType.IK_MAT);
    allergen.setCategory(TrainingCategory.ALLERGENS);
    allergen.setDescription("Training on allergen identification, labelling, and cross-contamination prevention.");
    allergen.setRequiredForRole(ResponsibleRole.ALL);
    allergen.setMandatory(true);
    allergen.setValidityDays(365);
    allergen.setAcknowledgmentRequired(true);
    trainingTemplateRepository.save(allergen);

    TrainingTemplate ageControl = new TrainingTemplate();
    ageControl.setOrganization(org);
    ageControl.setTitle("Age verification and ID check");
    ageControl.setModuleType(ModuleType.IK_ALKOHOL);
    ageControl.setCategory(TrainingCategory.AGE_CONTROL);
    ageControl.setDescription("Procedures for verifying customer age and handling ID checks.");
    ageControl.setRequiredForRole(ResponsibleRole.ALL);
    ageControl.setMandatory(true);
    ageControl.setValidityDays(180);
    ageControl.setAcknowledgmentRequired(true);
    trainingTemplateRepository.save(ageControl);

    TrainingTemplate intoxication = new TrainingTemplate();
    intoxication.setOrganization(org);
    intoxication.setTitle("Refusing service to intoxicated guests");
    intoxication.setModuleType(ModuleType.IK_ALKOHOL);
    intoxication.setCategory(TrainingCategory.INTOXICATION_HANDLING);
    intoxication.setDescription("Guidelines for identifying intoxicated guests and refusing service responsibly.");
    intoxication.setRequiredForRole(ResponsibleRole.ALL);
    intoxication.setMandatory(true);
    intoxication.setValidityDays(180);
    intoxication.setAcknowledgmentRequired(true);
    trainingTemplateRepository.save(intoxication);

    log.info("Seeded 4 training templates");
  }
}
