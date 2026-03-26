package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.CreateDishRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishAllergenOverrideRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishIngredientEntry;
import stud.ntnu.no.fullstack_project.dto.allergen.DishResponse;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishAllergenOverride;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.DishAllergenOverrideRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {

  @Mock
  private DishRepository dishRepository;

  @Mock
  private IngredientRepository ingredientRepository;

  @Mock
  private DishIngredientRepository dishIngredientRepository;

  @Mock
  private DishAllergenOverrideRepository dishAllergenOverrideRepository;

  @Mock
  private AllergenRepository allergenRepository;

  @InjectMocks
  private DishService dishService;

  private AppUser testUser;
  private Organization testOrg;
  private Allergen glutenAllergen;
  private Allergen milkAllergen;
  private Allergen peanutsAllergen;
  private Ingredient wheatFlour;
  private Ingredient parmesan;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("admin");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);

    glutenAllergen = new Allergen();
    glutenAllergen.setId(1L);
    glutenAllergen.setCode("GLUTEN");
    glutenAllergen.setNameNo("Gluten");
    glutenAllergen.setNameEn("Gluten");
    glutenAllergen.setActive(true);

    milkAllergen = new Allergen();
    milkAllergen.setId(7L);
    milkAllergen.setCode("MILK");
    milkAllergen.setNameNo("Melk");
    milkAllergen.setNameEn("Milk");
    milkAllergen.setActive(true);

    peanutsAllergen = new Allergen();
    peanutsAllergen.setId(5L);
    peanutsAllergen.setCode("PEANUTS");
    peanutsAllergen.setNameNo("Pean\u00f8tter");
    peanutsAllergen.setNameEn("Peanuts");
    peanutsAllergen.setActive(true);

    wheatFlour = new Ingredient();
    wheatFlour.setId(1L);
    wheatFlour.setName("Wheat Flour");
    wheatFlour.setOrganization(testOrg);
    wheatFlour.setAllergens(new HashSet<>(Set.of(glutenAllergen)));

    parmesan = new Ingredient();
    parmesan.setId(2L);
    parmesan.setName("Parmesan");
    parmesan.setOrganization(testOrg);
    parmesan.setAllergens(new HashSet<>(Set.of(milkAllergen)));
  }

  // --- Helper methods ---

  private Dish buildDish(Long id, String name) {
    Dish dish = new Dish();
    dish.setId(id);
    dish.setOrganization(testOrg);
    dish.setName(name);
    dish.setActive(true);
    dish.setCreatedAt(LocalDateTime.now());
    dish.setUpdatedAt(LocalDateTime.now());
    return dish;
  }

  private DishIngredient buildDishIngredient(Dish dish, Ingredient ingredient, String qty) {
    DishIngredient di = new DishIngredient();
    di.setId((long) (dish.getId() * 100 + ingredient.getId()));
    di.setDish(dish);
    di.setIngredient(ingredient);
    di.setQuantityText(qty);
    return di;
  }

  // --- computeDerivedAllergens tests ---

  @Test
  void computeDerivedAllergens_noIngredients_returnsEmpty() {
    Dish dish = buildDish(1L, "Empty Dish");

    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());

    List<AllergenResponse> result = dishService.computeDerivedAllergens(dish);

    assertTrue(result.isEmpty());
  }

  @Test
  void computeDerivedAllergens_withIngredients_returnsUnionOfAllergens() {
    Dish dish = buildDish(1L, "Caesar Salad");
    DishIngredient di1 = buildDishIngredient(dish, wheatFlour, "100g");
    DishIngredient di2 = buildDishIngredient(dish, parmesan, "50g");

    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of(di1, di2));
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());
    when(allergenRepository.findAllById(Set.of(1L, 7L)))
        .thenReturn(List.of(glutenAllergen, milkAllergen));

    List<AllergenResponse> result = dishService.computeDerivedAllergens(dish);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(a -> a.code().equals("GLUTEN")));
    assertTrue(result.stream().anyMatch(a -> a.code().equals("MILK")));
  }

  @Test
  void computeDerivedAllergens_withExclusionOverride_removesAllergen() {
    Dish dish = buildDish(1L, "GF Pasta");
    DishIngredient di1 = buildDishIngredient(dish, wheatFlour, "100g");

    DishAllergenOverride override = new DishAllergenOverride();
    override.setId(1L);
    override.setDish(dish);
    override.setAllergen(glutenAllergen);
    override.setIncluded(false);
    override.setReason("Uses gluten-free flour substitute");

    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of(di1));
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of(override));

    List<AllergenResponse> result = dishService.computeDerivedAllergens(dish);

    assertTrue(result.isEmpty());
  }

  @Test
  void computeDerivedAllergens_withInclusionOverride_addsAllergen() {
    Dish dish = buildDish(1L, "Peanut Dish");
    DishIngredient di1 = buildDishIngredient(dish, wheatFlour, "100g");

    DishAllergenOverride override = new DishAllergenOverride();
    override.setId(1L);
    override.setDish(dish);
    override.setAllergen(peanutsAllergen);
    override.setIncluded(true);
    override.setReason("Cross-contamination risk from shared equipment");

    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of(di1));
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of(override));
    when(allergenRepository.findAllById(Set.of(1L, 5L)))
        .thenReturn(List.of(glutenAllergen, peanutsAllergen));

    List<AllergenResponse> result = dishService.computeDerivedAllergens(dish);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(a -> a.code().equals("GLUTEN")));
    assertTrue(result.stream().anyMatch(a -> a.code().equals("PEANUTS")));
  }

  // --- createDish tests ---

  @Test
  void createDish_validInput_createsDishWithIngredients() {
    CreateDishRequest request = new CreateDishRequest(
        "Caesar Salad", "Classic salad", null,
        List.of(new DishIngredientEntry(1L, "100g"), new DishIngredientEntry(2L, "50g"))
    );

    when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
      Dish saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });
    when(ingredientRepository.findById(1L)).thenReturn(Optional.of(wheatFlour));
    when(ingredientRepository.findById(2L)).thenReturn(Optional.of(parmesan));
    when(dishIngredientRepository.save(any(DishIngredient.class))).thenAnswer(i -> i.getArgument(0));
    when(dishIngredientRepository.findByDishId(10L)).thenReturn(List.of(
        buildDishIngredient(buildDish(10L, "Caesar Salad"), wheatFlour, "100g"),
        buildDishIngredient(buildDish(10L, "Caesar Salad"), parmesan, "50g")
    ));
    when(dishAllergenOverrideRepository.findByDishId(10L)).thenReturn(List.of());
    when(allergenRepository.findAllById(Set.of(1L, 7L)))
        .thenReturn(List.of(glutenAllergen, milkAllergen));

    DishResponse response = dishService.createDish(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("Caesar Salad", response.name());
    assertEquals(2, response.ingredients().size());
    assertEquals(2, response.derivedAllergens().size());
    assertTrue(response.changedSinceApproval());
    assertNull(response.lastApprovedAt());
  }

  // --- approveDish tests ---

  @Test
  void approveDish_setsApprovalFields() {
    Dish dish = buildDish(1L, "Test Dish");
    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(dishRepository.save(any(Dish.class))).thenAnswer(i -> {
      Dish saved = i.getArgument(0);
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());

    DishResponse response = dishService.approveDish(1L, testUser);

    assertNotNull(response);
    assertNotNull(response.lastApprovedAt());
    assertEquals("admin", response.lastApprovedByUsername());
  }

  // --- addOverride tests ---

  @Test
  void addOverride_addsOverrideToDish() {
    Dish dish = buildDish(1L, "Test Dish");
    DishAllergenOverrideRequest request = new DishAllergenOverrideRequest(
        1L, false, "Uses gluten-free substitute"
    );

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(allergenRepository.findById(1L)).thenReturn(Optional.of(glutenAllergen));
    when(dishAllergenOverrideRepository.save(any(DishAllergenOverride.class)))
        .thenAnswer(i -> {
          DishAllergenOverride saved = i.getArgument(0);
          saved.setId(100L);
          return saved;
        });
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());

    DishAllergenOverride savedOverride = new DishAllergenOverride();
    savedOverride.setId(100L);
    savedOverride.setDish(dish);
    savedOverride.setAllergen(glutenAllergen);
    savedOverride.setIncluded(false);
    savedOverride.setReason("Uses gluten-free substitute");
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of(savedOverride));

    DishResponse response = dishService.addOverride(1L, request);

    assertNotNull(response);
    assertEquals(1, response.overrides().size());
    assertFalse(response.overrides().get(0).included());
    assertEquals("Uses gluten-free substitute", response.overrides().get(0).reason());
  }

  // --- removeOverride tests ---

  @Test
  void removeOverride_removesOverrideFromDish() {
    Dish dish = buildDish(1L, "Test Dish");

    DishAllergenOverride override = new DishAllergenOverride();
    override.setId(100L);
    override.setDish(dish);
    override.setAllergen(glutenAllergen);
    override.setIncluded(false);
    override.setReason("Some reason");

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(dishAllergenOverrideRepository.findById(100L)).thenReturn(Optional.of(override));
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());

    DishResponse response = dishService.removeOverride(1L, 100L);

    assertNotNull(response);
    assertTrue(response.overrides().isEmpty());
    verify(dishAllergenOverrideRepository).delete(override);
  }

  @Test
  void removeOverride_wrongDish_throwsIllegalArgumentException() {
    Dish dish1 = buildDish(1L, "Dish 1");
    Dish dish2 = buildDish(2L, "Dish 2");

    DishAllergenOverride override = new DishAllergenOverride();
    override.setId(100L);
    override.setDish(dish2);
    override.setAllergen(glutenAllergen);

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish1));
    when(dishAllergenOverrideRepository.findById(100L)).thenReturn(Optional.of(override));

    assertThrows(IllegalArgumentException.class,
        () -> dishService.removeOverride(1L, 100L));
  }

  // --- getDish tests ---

  @Test
  void getDish_nonExistentId_throwsIllegalArgumentException() {
    when(dishRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> dishService.getDish(999L));
  }

  @Test
  void getDish_returnsDishWithDerivedAllergens() {
    Dish dish = buildDish(1L, "Caesar Salad");
    DishIngredient di1 = buildDishIngredient(dish, wheatFlour, "100g");
    DishIngredient di2 = buildDishIngredient(dish, parmesan, "50g");

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of(di1, di2));
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());
    when(allergenRepository.findAllById(Set.of(1L, 7L)))
        .thenReturn(List.of(glutenAllergen, milkAllergen));

    DishResponse response = dishService.getDish(1L);

    assertNotNull(response);
    assertEquals("Caesar Salad", response.name());
    assertEquals(2, response.ingredients().size());
    assertEquals(2, response.derivedAllergens().size());
  }

  // --- changedSinceApproval tests ---

  @Test
  void changedSinceApproval_neverApproved_returnsTrue() {
    Dish dish = buildDish(1L, "Unapproved Dish");
    dish.setLastApprovedAt(null);

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());

    DishResponse response = dishService.getDish(1L);

    assertTrue(response.changedSinceApproval());
  }

  @Test
  void changedSinceApproval_updatedAfterApproval_returnsTrue() {
    Dish dish = buildDish(1L, "Modified Dish");
    dish.setLastApprovedAt(LocalDateTime.now().minusHours(1));
    dish.setLastApprovedBy(testUser);
    dish.setUpdatedAt(LocalDateTime.now());

    when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
    when(dishIngredientRepository.findByDishId(1L)).thenReturn(List.of());
    when(dishAllergenOverrideRepository.findByDishId(1L)).thenReturn(List.of());

    DishResponse response = dishService.getDish(1L);

    assertTrue(response.changedSinceApproval());
  }
}
