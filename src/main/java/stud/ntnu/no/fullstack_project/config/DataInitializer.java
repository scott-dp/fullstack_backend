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
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.entity.Supplier;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;
import stud.ntnu.no.fullstack_project.repository.SupplierRepository;

/**
 * Seeds the database with a default organization and sample users on first run.
 *
 * <p>Only executes when the user table is empty, ensuring idempotent behaviour.
 * Creates one organization, three users (admin, manager, staff), the 14 EU
 * allergens, sample ingredients, and sample dishes for development and testing.</p>
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

    log.info("Seed data initialized: 1 organization, 3 users, 14 allergens, "
        + "3 ingredients, 3 dishes");
  }

  private void seedAllergens() {
    if (allergenRepository.count() > 0) return;
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
      Allergen a = new Allergen();
      a.setCode(row[0]);
      a.setNameNo(row[1]);
      a.setNameEn(row[2]);
      allergenRepository.save(a);
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

    // Seed ingredients
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

    // Seed dishes
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
    DishIngredient di = new DishIngredient();
    di.setDish(dish);
    di.setIngredient(ingredient);
    di.setQuantityText(quantityText);
    dishIngredientRepository.save(di);
  }

  private void seedSuppliers(Organization org) {
    Supplier s1 = new Supplier();
    s1.setOrganization(org);
    s1.setName("Norsk Sjømat AS");
    s1.setOrganizationNumber("912345678");
    s1.setContactName("Erik Hansen");
    s1.setEmail("ordre@norsksjoemat.no");
    s1.setPhone("+47 22 33 44 55");
    s1.setAddress("Aker Brygge 12, 0250 Oslo");
    s1.setNotes("Main seafood supplier. Delivers Mon/Wed/Fri.");
    supplierRepository.save(s1);

    Supplier s2 = new Supplier();
    s2.setOrganization(org);
    s2.setName("Oslo Drikke AS");
    s2.setOrganizationNumber("987654321");
    s2.setContactName("Maria Olsen");
    s2.setEmail("salg@oslodrikke.no");
    s2.setPhone("+47 55 66 77 88");
    s2.setAddress("Grünerløkka 5, 0555 Oslo");
    s2.setNotes("Beverage supplier for soft drinks and alcohol.");
    supplierRepository.save(s2);
  }
}
