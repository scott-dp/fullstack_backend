package stud.ntnu.no.fullstack_project.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.AlcoholGroup;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Bevilling;
import stud.ntnu.no.fullstack_project.entity.BevillingCondition;
import stud.ntnu.no.fullstack_project.entity.BevillingServingHours;
import stud.ntnu.no.fullstack_project.entity.BevillingStatus;
import stud.ntnu.no.fullstack_project.entity.BevillingType;
import stud.ntnu.no.fullstack_project.entity.ConditionType;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.FrequencyType;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.entity.Routine;
import stud.ntnu.no.fullstack_project.entity.RoutineCategory;
import stud.ntnu.no.fullstack_project.entity.Supplier;
import stud.ntnu.no.fullstack_project.entity.TrainingCategory;
import stud.ntnu.no.fullstack_project.entity.TrainingTemplate;
import stud.ntnu.no.fullstack_project.entity.Weekday;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingConditionRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingServingHoursRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;
import stud.ntnu.no.fullstack_project.repository.RoutineRepository;
import stud.ntnu.no.fullstack_project.repository.SupplierRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingTemplateRepository;

/**
 * Seeds the database with a default organization, users, and demo data on first run.
 *
 * <p>Creates one bootstrap superadmin plus one demo organization with users, allergens, ingredients,
 * dishes, suppliers, training templates, routines, and a bevilling with conditions and serving
 * hours.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final AppUserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final AllergenRepository allergenRepository;
  private final IngredientRepository ingredientRepository;
  private final DishRepository dishRepository;
  private final DishIngredientRepository dishIngredientRepository;
  private final RoutineRepository routineRepository;
  private final SupplierRepository supplierRepository;
  private final TrainingTemplateRepository trainingTemplateRepository;
  private final PasswordEncoder passwordEncoder;
  private final BevillingRepository bevillingRepository;
  private final BevillingConditionRepository bevillingConditionRepository;
  private final BevillingServingHoursRepository bevillingServingHoursRepository;

  @Override
  public void run(String... args) {
    seedSuperAdmin();

    if (organizationRepository.count() > 0) {
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

    seedAllergens();
    seedSuppliers(org);
    seedAllergenData(org);
    seedTrainingTemplates(org);
    seedRoutines(org, admin);
    seedBevilling(org);

    log.info(
        "Seed data initialized: 1 organization, 3 users, 14 allergens, 4 ingredients, 3 dishes, "
            + "2 suppliers, 4 training templates, 11 routines, 1 bevilling");
  }

  private void seedSuperAdmin() {
    if (userRepository.existsByRolesContaining(Role.ROLE_SUPERADMIN)) {
      return;
    }

    AppUser superAdmin = new AppUser();
    superAdmin.setUsername("superadmin");
    superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
    superAdmin.setFirstName("Platform");
    superAdmin.setLastName("Superadmin");
    superAdmin.setEmail("superadmin@iksystem.no");
    superAdmin.setEmailVerified(true);
    superAdmin.setEnabled(true);
    superAdmin.setRoles(Set.of(Role.ROLE_SUPERADMIN));
    userRepository.save(superAdmin);

    log.info("Seeded default superadmin account username=superadmin");
  }

  private void seedAllergens() {
    if (allergenRepository.count() > 0) {
      return;
    }

    String[][] data = {
        {"GLUTEN", "Gluten", "Gluten"},
        {"CRUSTACEANS", "Krepsdyr", "Crustaceans"},
        {"EGGS", "Egg", "Eggs"},
        {"FISH", "Fisk", "Fish"},
        {"PEANUTS", "Peanøtter", "Peanuts"},
        {"SOYBEANS", "Soya", "Soybeans"},
        {"MILK", "Melk", "Milk"},
        {"NUTS", "Nøtter", "Nuts"},
        {"CELERY", "Selleri", "Celery"},
        {"MUSTARD", "Sennep", "Mustard"},
        {"SESAME", "Sesam", "Sesame"},
        {"SULPHITES", "Sulfitter", "Sulphites"},
        {"LUPIN", "Lupin", "Lupin"},
        {"MOLLUSCS", "Bløtdyr", "Molluscs"},
    };

    for (String[] row : data) {
      Allergen allergen = new Allergen();
      allergen.setCode(row[0]);
      allergen.setNameNo(row[1]);
      allergen.setNameEn(row[2]);
      allergenRepository.save(allergen);
    }

    log.info("Seeded 14 EU allergens");
  }

  private void seedAllergenData(Organization org) {
    if (ingredientRepository.count() > 0) {
      return;
    }

    Allergen milk = allergenRepository.findByCode("MILK").orElseThrow();
    Allergen gluten = allergenRepository.findByCode("GLUTEN").orElseThrow();
    Allergen peanuts = allergenRepository.findByCode("PEANUTS").orElseThrow();
    Allergen fish = allergenRepository.findByCode("FISH").orElseThrow();

    Ingredient parmesan = new Ingredient();
    parmesan.setOrganization(org);
    parmesan.setName("Parmesan");
    parmesan.setAllergens(new HashSet<>(Set.of(milk)));
    parmesan = ingredientRepository.save(parmesan);

    Ingredient wheatFlour = new Ingredient();
    wheatFlour.setOrganization(org);
    wheatFlour.setName("Wheat Flour");
    wheatFlour.setAllergens(new HashSet<>(Set.of(gluten)));
    wheatFlour = ingredientRepository.save(wheatFlour);

    Ingredient peanutButter = new Ingredient();
    peanutButter.setOrganization(org);
    peanutButter.setName("Peanut Butter");
    peanutButter.setAllergens(new HashSet<>(Set.of(peanuts)));
    peanutButter = ingredientRepository.save(peanutButter);

    Ingredient salmon = new Ingredient();
    salmon.setOrganization(org);
    salmon.setName("Salmon");
    salmon.setAllergens(new HashSet<>(Set.of(fish)));
    salmon = ingredientRepository.save(salmon);

    log.info("Seeded 4 example ingredients");

    Dish caesarSalad = new Dish();
    caesarSalad.setOrganization(org);
    caesarSalad.setName("Caesar Salad");
    caesarSalad.setDescription("Classic caesar salad with parmesan and croutons");
    caesarSalad = dishRepository.save(caesarSalad);
    addDishIngredient(caesarSalad, parmesan, "50g");
    addDishIngredient(caesarSalad, wheatFlour, "Croutons, 30g");

    Dish fishSoup = new Dish();
    fishSoup.setOrganization(org);
    fishSoup.setName("Fish Soup");
    fishSoup.setDescription("Traditional Norwegian fish soup with salmon");
    fishSoup = dishRepository.save(fishSoup);
    addDishIngredient(fishSoup, salmon, "200g");

    Dish peanutBrownie = new Dish();
    peanutBrownie.setOrganization(org);
    peanutBrownie.setName("Peanut Brownie");
    peanutBrownie.setDescription("Rich chocolate brownie with peanut butter swirl");
    peanutBrownie = dishRepository.save(peanutBrownie);
    addDishIngredient(peanutBrownie, peanutButter, "100g");
    addDishIngredient(peanutBrownie, wheatFlour, "150g");

    log.info("Seeded 3 example dishes");
  }

  private void addDishIngredient(Dish dish, Ingredient ingredient, String quantityText) {
    DishIngredient dishIngredient = new DishIngredient();
    dishIngredient.setDish(dish);
    dishIngredient.setIngredient(ingredient);
    dishIngredient.setQuantityText(quantityText);
    dishIngredientRepository.save(dishIngredient);
  }

  private void seedSuppliers(Organization org) {
    if (supplierRepository.count() > 0) {
      return;
    }

    Supplier seafoodSupplier = new Supplier();
    seafoodSupplier.setOrganization(org);
    seafoodSupplier.setName("Norsk Sjømat AS");
    seafoodSupplier.setOrganizationNumber("912345678");
    seafoodSupplier.setContactName("Erik Hansen");
    seafoodSupplier.setEmail("ordre@norsksjoemat.no");
    seafoodSupplier.setPhone("+47 22 33 44 55");
    seafoodSupplier.setAddress("Aker Brygge 12, 0250 Oslo");
    seafoodSupplier.setNotes("Main seafood supplier. Delivers Mon/Wed/Fri.");
    supplierRepository.save(seafoodSupplier);

    Supplier beverageSupplier = new Supplier();
    beverageSupplier.setOrganization(org);
    beverageSupplier.setName("Oslo Drikke AS");
    beverageSupplier.setOrganizationNumber("987654321");
    beverageSupplier.setContactName("Maria Olsen");
    beverageSupplier.setEmail("salg@oslodrikke.no");
    beverageSupplier.setPhone("+47 55 66 77 88");
    beverageSupplier.setAddress("Grünerløkka 5, 0555 Oslo");
    beverageSupplier.setNotes("Beverage supplier for soft drinks and alcohol.");
    supplierRepository.save(beverageSupplier);

    log.info("Seeded 2 suppliers");
  }

  private void seedTrainingTemplates(Organization org) {
    if (trainingTemplateRepository.count() > 0) {
      return;
    }

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
    allergen.setDescription(
        "Training on allergen identification, labelling, and cross-contamination prevention.");
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
    intoxication.setDescription(
        "Guidelines for identifying intoxicated guests and refusing service responsibly.");
    intoxication.setRequiredForRole(ResponsibleRole.ALL);
    intoxication.setMandatory(true);
    intoxication.setValidityDays(180);
    intoxication.setAcknowledgmentRequired(true);
    trainingTemplateRepository.save(intoxication);

    log.info("Seeded 4 training templates");
  }

  private void seedRoutines(Organization org, AppUser admin) {
    if (routineRepository.count() > 0) {
      return;
    }

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
        "Monitoring intoxication during shift", ModuleType.IK_ALKOHOL,
        RoutineCategory.INTOXICATION,
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

  private void seedBevilling(Organization org) {
    if (bevillingRepository.count() > 0) {
      return;
    }

    Bevilling bevilling = new Bevilling();
    bevilling.setOrganization(org);
    bevilling.setMunicipality("Trondheim");
    bevilling.setBevillingType(BevillingType.SKJENKING);
    bevilling.setValidFrom(LocalDate.of(2025, 1, 1));
    bevilling.setValidTo(LocalDate.of(2027, 12, 31));
    bevilling.setLicenseNumber("SK-2025-001");
    bevilling.setStatus(BevillingStatus.ACTIVE);
    bevilling.setAlcoholGroupsAllowed(
        Set.of(AlcoholGroup.GROUP_1, AlcoholGroup.GROUP_2, AlcoholGroup.GROUP_3));
    bevilling.setServingAreaDescription("Main dining room, bar area, and outdoor terrace");
    bevilling.setIndoorAllowed(true);
    bevilling.setOutdoorAllowed(true);
    bevilling.setStyrerName("Restaurant Manager");
    bevilling.setStedfortrederName("System Administrator");
    bevilling.setNotes("Renewed annually by Trondheim kommune");
    bevilling = bevillingRepository.save(bevilling);

    BevillingCondition cond1 = new BevillingCondition();
    cond1.setBevilling(bevilling);
    cond1.setConditionType(ConditionType.FOOD_REQUIREMENT);
    cond1.setTitle("Food must be available");
    cond1.setDescription("Hot food must be available during all serving hours.");
    cond1.setActive(true);
    bevillingConditionRepository.save(cond1);

    BevillingCondition cond2 = new BevillingCondition();
    cond2.setBevilling(bevilling);
    cond2.setConditionType(ConditionType.TRAINING_REQUIREMENT);
    cond2.setTitle("Staff training required");
    cond2.setDescription("All serving staff must complete responsible alcohol service training.");
    cond2.setActive(true);
    bevillingConditionRepository.save(cond2);

    for (Weekday day : Weekday.values()) {
      BevillingServingHours hours = new BevillingServingHours();
      hours.setBevilling(bevilling);
      hours.setWeekday(day);
      if (day == Weekday.FRI || day == Weekday.SAT) {
        hours.setStartTime(LocalTime.of(11, 0));
        hours.setEndTime(LocalTime.of(2, 0));
      } else if (day == Weekday.SUN) {
        hours.setStartTime(LocalTime.of(12, 0));
        hours.setEndTime(LocalTime.of(22, 0));
      } else {
        hours.setStartTime(LocalTime.of(11, 0));
        hours.setEndTime(LocalTime.of(0, 0));
      }
      hours.setConsumptionDeadlineMinutesAfterEnd(30);
      bevillingServingHoursRepository.save(hours);
    }
  }
}
