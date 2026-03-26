package stud.ntnu.no.fullstack_project.config;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.entity.Supplier;
import stud.ntnu.no.fullstack_project.entity.TrainingCategory;
import stud.ntnu.no.fullstack_project.entity.TrainingTemplate;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;
import stud.ntnu.no.fullstack_project.repository.SupplierRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingTemplateRepository;

/**
 * Seeds the database with a default organization and sample users on first run.
 *
 * <p>Only executes when the user table is empty, ensuring idempotent behaviour.
 * Creates one organization, three users (admin, manager, staff), the 14 EU
 * allergens, sample ingredients and dishes, sample suppliers, and default
 * training templates for development and testing.</p>
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
  private final SupplierRepository supplierRepository;
  private final TrainingTemplateRepository trainingTemplateRepository;
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

    seedAllergens();
    seedSuppliers(org);
    seedAllergenData(org);
    seedTrainingTemplates(org);

    log.info("Seed data initialized: 1 organization, 3 users, 14 allergens, "
        + "3 ingredients, 3 dishes, suppliers, training templates");
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

  /**
   * Seeds example ingredients and dishes for the given organization.
   *
   * @param org the organization to associate ingredients and dishes with
   */
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

  /**
   * Seeds default training templates for the given organization.
   *
   * @param org the organization to create training templates for
   */
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
}
