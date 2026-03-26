package stud.ntnu.no.fullstack_project.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.CreateIngredientRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.IngredientResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.UpdateIngredientRequest;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;

/**
 * Service for managing ingredients and their allergen associations.
 *
 * <p>Provides CRUD operations for ingredients within an organization,
 * including linking ingredients to allergens from the EU-14 reference list.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

  private final IngredientRepository ingredientRepository;
  private final AllergenRepository allergenRepository;
  private final DishIngredientRepository dishIngredientRepository;
  private final DishRepository dishRepository;

  /**
   * Creates a new ingredient with allergen associations.
   *
   * @param request     the ingredient creation details
   * @param currentUser the authenticated user creating the ingredient
   * @return the created ingredient response
   */
  @Transactional
  public IngredientResponse createIngredient(CreateIngredientRequest request,
      AppUser currentUser) {
    Ingredient ingredient = new Ingredient();
    ingredient.setOrganization(currentUser.getOrganization());
    ingredient.setName(request.name());
    ingredient.setNotes(request.notes());

    if (request.allergenIds() != null && !request.allergenIds().isEmpty()) {
      Set<Allergen> allergens = new HashSet<>(allergenRepository.findAllById(request.allergenIds()));
      ingredient.setAllergens(allergens);
    }

    Ingredient saved = ingredientRepository.save(ingredient);
    log.info("Ingredient created: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Updates an existing ingredient's name, notes, and allergen associations.
   *
   * @param id      the ingredient identifier
   * @param request the update details
   * @return the updated ingredient response
   */
  @Transactional
  public IngredientResponse updateIngredient(Long id, UpdateIngredientRequest request) {
    Ingredient ingredient = ingredientRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Ingredient not found with id: " + id));

    if (request.name() != null && !request.name().isBlank()) {
      ingredient.setName(request.name());
    }
    if (request.notes() != null) {
      ingredient.setNotes(request.notes());
    }
    if (request.allergenIds() != null) {
      Set<Allergen> allergens = new HashSet<>(allergenRepository.findAllById(request.allergenIds()));
      ingredient.setAllergens(allergens);
    }

    Ingredient saved = ingredientRepository.save(ingredient);
    invalidateAffectedDishApprovals(saved.getId());
    log.info("Ingredient updated: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a single ingredient by its ID, including its allergens.
   *
   * @param id the ingredient identifier
   * @return the ingredient response
   */
  public IngredientResponse getIngredient(Long id) {
    Ingredient ingredient = ingredientRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Ingredient not found with id: " + id));
    return mapToResponse(ingredient);
  }

  /**
   * Lists all ingredients for a given organization, ordered by name.
   *
   * @param orgId the organization identifier
   * @return list of ingredient responses
   */
  public List<IngredientResponse> listIngredients(Long orgId) {
    return ingredientRepository.findByOrganizationIdOrderByNameAsc(orgId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteIngredient(Long id) {
    Ingredient ingredient = ingredientRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Ingredient not found with id: " + id));

    invalidateAffectedDishApprovals(id);
    dishIngredientRepository.deleteByIngredientId(id);
    ingredientRepository.delete(ingredient);
    log.info("Ingredient deleted: id={}", id);
  }

  /**
   * Maps an ingredient entity to its response DTO.
   *
   * @param ingredient the ingredient entity
   * @return the ingredient response DTO
   */
  private IngredientResponse mapToResponse(Ingredient ingredient) {
    List<AllergenResponse> allergens = ingredient.getAllergens().stream()
        .map(a -> new AllergenResponse(a.getId(), a.getCode(), a.getNameNo(), a.getNameEn()))
        .collect(Collectors.toList());

    return new IngredientResponse(
        ingredient.getId(),
        ingredient.getName(),
        ingredient.getNotes(),
        allergens,
        ingredient.getCreatedAt(),
        ingredient.getUpdatedAt()
    );
  }

  private void invalidateAffectedDishApprovals(Long ingredientId) {
    List<DishIngredient> links = dishIngredientRepository.findByIngredientId(ingredientId);
    if (links.isEmpty()) {
      return;
    }

    List<Dish> affectedDishes = links.stream()
        .map(DishIngredient::getDish)
        .peek(dish -> dish.setAllergenApprovalValid(false))
        .toList();
    dishRepository.saveAll(affectedDishes);
  }
}
