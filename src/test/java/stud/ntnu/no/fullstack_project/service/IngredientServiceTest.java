package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.allergen.UpdateIngredientRequest;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

  @Mock
  private IngredientRepository ingredientRepository;

  @Mock
  private AllergenRepository allergenRepository;

  @Mock
  private DishIngredientRepository dishIngredientRepository;

  @Mock
  private DishRepository dishRepository;

  @InjectMocks
  private IngredientService ingredientService;

  private Ingredient ingredient;
  private Dish dish;
  private Allergen allergen;

  @BeforeEach
  void setUp() {
    allergen = new Allergen();
    allergen.setId(5L);
    allergen.setCode("PEANUTS");
    allergen.setNameEn("Peanuts");

    ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Peanut oil");

    dish = new Dish();
    dish.setId(10L);
    dish.setName("Noodles");
    dish.setAllergenApprovalValid(true);
  }

  @Test
  void updateIngredient_invalidatesApprovalForAffectedDishes() {
    DishIngredient dishIngredient = new DishIngredient();
    dishIngredient.setDish(dish);
    dishIngredient.setIngredient(ingredient);

    when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
    when(allergenRepository.findAllById(List.of(5L))).thenReturn(List.of(allergen));
    when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(dishIngredientRepository.findByIngredientId(1L)).thenReturn(List.of(dishIngredient));

    ingredientService.updateIngredient(
        1L,
        new UpdateIngredientRequest("Peanut oil", "Updated", List.of(5L))
    );

    assertFalse(dish.isAllergenApprovalValid());
    verify(dishRepository).saveAll(List.of(dish));
  }
}
