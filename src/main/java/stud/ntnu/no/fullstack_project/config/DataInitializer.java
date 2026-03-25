package stud.ntnu.no.fullstack_project.config;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.FrequencyType;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.entity.Routine;
import stud.ntnu.no.fullstack_project.entity.RoutineCategory;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;
import stud.ntnu.no.fullstack_project.repository.RoutineRepository;

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
  private final RoutineRepository routineRepository;
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

    seedRoutines(org, admin);

    log.info("Seed data initialized: 1 organization, 3 users, seed routines");
  }

  private void seedRoutines(Organization org, AppUser admin) {
    routineRepository.save(buildRoutine(org, admin,
        "Morning fridge temperature control", ModuleType.IK_MAT, RoutineCategory.TEMPERATURE,
        "Check all fridge/cooler units before service starts.",
        "Ensure food is stored at safe temperatures at all times.",
        ResponsibleRole.STAFF, FrequencyType.DAILY,
        "1. Open each fridge and read thermometer\n2. Record temperature in system\n3. Flag any reading above 4°C",
        "Temperature above 4°C in any fridge unit.",
        "Adjust thermostat, move food to working unit, notify manager.",
        "Photo of thermometer reading.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Freezer temperature control", ModuleType.IK_MAT, RoutineCategory.TEMPERATURE,
        "Check freezer temperatures daily.",
        "Ensure frozen goods remain at safe storage temperatures.",
        ResponsibleRole.STAFF, FrequencyType.DAILY,
        "1. Check each freezer display\n2. Record in system\n3. Flag any reading above -18°C",
        "Temperature above -18°C.", "Contact maintenance, relocate items.",
        "Temperature log entry.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Daily cleaning of prep surfaces", ModuleType.IK_MAT, RoutineCategory.CLEANING,
        "Clean and sanitize all food preparation surfaces after each shift.",
        "Prevent cross-contamination and maintain hygiene.",
        ResponsibleRole.STAFF, FrequencyType.DAILY,
        "1. Clear surface\n2. Wash with hot soapy water\n3. Sanitize\n4. Air dry",
        "Surfaces not cleaned between different food types.",
        "Re-clean immediately, re-train staff.", "Completed checklist.", 90));

    routineRepository.save(buildRoutine(org, admin,
        "Weekly deep cleaning", ModuleType.IK_MAT, RoutineCategory.CLEANING,
        "Deep clean all kitchen areas weekly.",
        "Maintain overall kitchen hygiene standards.",
        ResponsibleRole.MANAGER, FrequencyType.WEEKLY,
        "1. Disassemble equipment\n2. Deep clean all surfaces\n3. Clean drains\n4. Inspect for pests",
        "Areas missed or not properly cleaned.",
        "Re-clean and document.", "Signed cleaning log.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Allergen information review", ModuleType.IK_MAT, RoutineCategory.ALLERGENS,
        "Review allergen information for all menu items.",
        "Ensure accurate allergen information is available for customers.",
        ResponsibleRole.MANAGER, FrequencyType.MONTHLY,
        "1. Review ingredient lists\n2. Update allergen matrix\n3. Print updated allergen sheet",
        "Missing or outdated allergen information.",
        "Update records immediately, inform staff.", "Updated allergen sheet.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Goods receiving and traceability", ModuleType.IK_MAT, RoutineCategory.TRACEABILITY,
        "Inspect and log all incoming deliveries.",
        "Maintain traceability and reject non-conforming goods.",
        ResponsibleRole.STAFF, FrequencyType.EVENT_BASED,
        "1. Check delivery against order\n2. Inspect temperatures\n3. Record batch/lot numbers\n4. Store properly",
        "Damaged goods, wrong temperature, missing documentation.",
        "Reject delivery, notify supplier, document.", "Delivery log with signature.", 90));

    routineRepository.save(buildRoutine(org, admin,
        "HACCP hazard review for hot holding", ModuleType.IK_MAT, RoutineCategory.HACCP,
        "Verify hot-held food stays above 60°C.",
        "Prevent bacterial growth in hot-held food.",
        ResponsibleRole.STAFF, FrequencyType.SHIFT_BASED,
        "1. Check temperature of each hot-held item\n2. Record readings\n3. Discard food below 60°C held for over 2 hours",
        "Temperature below 60°C.", "Reheat to 75°C or discard.",
        "Temperature readings.", 30));

    // IK-Alkohol routines
    routineRepository.save(buildRoutine(org, admin,
        "ID check at bar", ModuleType.IK_ALKOHOL, RoutineCategory.AGE_CONTROL,
        "Verify age of all customers who appear under 25.",
        "Prevent service of alcohol to minors.",
        ResponsibleRole.STAFF, FrequencyType.SHIFT_BASED,
        "1. Ask for valid ID\n2. Check photo matches\n3. Verify date of birth\n4. Refuse if under 18 or no valid ID",
        "Serving alcohol without checking ID when required.",
        "Refuse service, log incident, notify manager.",
        "Incident log entry.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Monitoring intoxication during shift", ModuleType.IK_ALKOHOL, RoutineCategory.INTOXICATION,
        "Observe guests for signs of intoxication throughout service.",
        "Comply with alcohol service laws and ensure guest safety.",
        ResponsibleRole.STAFF, FrequencyType.SHIFT_BASED,
        "1. Observe guest behavior\n2. Assess level of intoxication\n3. Refuse further service if intoxicated\n4. Log refusals",
        "Continuing to serve visibly intoxicated guests.",
        "Stop service immediately, offer water/food, call taxi if needed.",
        "Refusal log entry.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "End-of-service closing control", ModuleType.IK_ALKOHOL, RoutineCategory.CLOSING,
        "Ensure all alcohol service stops at permitted closing time.",
        "Comply with bevilling serving hour conditions.",
        ResponsibleRole.MANAGER, FrequencyType.DAILY,
        "1. Announce last call 30 min before closing\n2. Stop serving at closing time\n3. Allow consumption deadline\n4. Clear premises",
        "Alcohol served after permitted hours.",
        "Stop immediately, document, review procedures.",
        "Closing checklist.", 30));

    routineRepository.save(buildRoutine(org, admin,
        "Preventing alcohol taken outside serving area", ModuleType.IK_ALKOHOL,
        RoutineCategory.LICENSE_CONDITIONS,
        "Ensure guests do not take drinks outside the licensed serving area.",
        "Comply with bevilling area conditions.",
        ResponsibleRole.STAFF, FrequencyType.SHIFT_BASED,
        "1. Monitor exits\n2. Remind guests of serving area boundaries\n3. Collect glasses near exits",
        "Guests taking drinks outside permitted area.",
        "Ask guest to return, collect drink, log if repeated.",
        "Incident log.", 90));

    log.info("Seeded {} routines", 11);
  }

  private Routine buildRoutine(Organization org, AppUser createdBy, String name,
      ModuleType moduleType, RoutineCategory category, String description,
      String purpose, ResponsibleRole role, FrequencyType freq,
      String steps, String deviationText, String correctiveAction,
      String evidence, int reviewDays) {
    Routine r = new Routine();
    r.setOrganization(org);
    r.setName(name);
    r.setModuleType(moduleType);
    r.setCategory(category);
    r.setDescription(description);
    r.setPurpose(purpose);
    r.setResponsibleRole(role);
    r.setFrequencyType(freq);
    r.setStepsText(steps);
    r.setWhatIsDeviationText(deviationText);
    r.setCorrectiveActionText(correctiveAction);
    r.setRequiredEvidenceText(evidence);
    r.setReviewIntervalDays(reviewDays);
    r.setCreatedBy(createdBy);
    return r;
  }
}
