package stud.ntnu.no.fullstack_project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenSheetEntry;
import stud.ntnu.no.fullstack_project.dto.allergen.CreateDishRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishAllergenOverrideRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishAllergenOverrideResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.DishIngredientEntry;
import stud.ntnu.no.fullstack_project.dto.allergen.DishIngredientResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.DishResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.UpdateDishRequest;
import stud.ntnu.no.fullstack_project.entity.Allergen;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Dish;
import stud.ntnu.no.fullstack_project.entity.DishAllergenOverride;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;
import stud.ntnu.no.fullstack_project.entity.Ingredient;
import stud.ntnu.no.fullstack_project.repository.AllergenRepository;
import stud.ntnu.no.fullstack_project.repository.DishAllergenOverrideRepository;
import stud.ntnu.no.fullstack_project.repository.DishIngredientRepository;
import stud.ntnu.no.fullstack_project.repository.DishRepository;
import stud.ntnu.no.fullstack_project.repository.IngredientRepository;

/**
 * Service for managing dishes, their ingredient composition, allergen derivation, and overrides.
 *
 * <p>Handles creation, retrieval, approval, and allergen override workflows for dishes.
 * Derived allergens are computed by collecting all allergens from a dish's ingredients
 * and then applying any overrides (additions or exclusions).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DishService {

  private final DishRepository dishRepository;
  private final IngredientRepository ingredientRepository;
  private final DishIngredientRepository dishIngredientRepository;
  private final DishAllergenOverrideRepository dishAllergenOverrideRepository;
  private final AllergenRepository allergenRepository;

  /**
   * Creates a new dish with ingredient links.
   *
   * @param request     the dish creation details
   * @param currentUser the authenticated user creating the dish
   * @return the created dish response
   */
  @Transactional
  public DishResponse createDish(CreateDishRequest request, AppUser currentUser) {
    Dish dish = new Dish();
    dish.setOrganization(currentUser.getOrganization());
    dish.setName(request.name());
    dish.setDescription(request.description());
    dish.setNotes(request.notes());
    dish.setAllergenApprovalValid(false);
    dish = dishRepository.save(dish);

    if (request.ingredientIds() != null) {
      saveDishIngredients(dish, request.ingredientIds());
    }

    log.info("Dish created: {} (id={})", dish.getName(), dish.getId());
    return buildDishResponse(dish);
  }

  /**
   * Updates an existing dish's name, description, notes, and ingredient links.
   *
   * @param id      the dish identifier
   * @param request the update details
   * @return the updated dish response
   */
  @Transactional
  public DishResponse updateDish(Long id, UpdateDishRequest request) {
    Dish dish = dishRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + id));

    if (request.name() != null && !request.name().isBlank()) {
      dish.setName(request.name());
    }
    if (request.description() != null) {
      dish.setDescription(request.description());
    }
    if (request.notes() != null) {
      dish.setNotes(request.notes());
    }
    if (request.ingredientIds() != null) {
      dishIngredientRepository.deleteByDishId(dish.getId());
      saveDishIngredients(dish, request.ingredientIds());
    }

    invalidateApproval(dish);

    dish = dishRepository.save(dish);
    log.info("Dish updated: {} (id={})", dish.getName(), dish.getId());
    return buildDishResponse(dish);
  }

  /**
   * Retrieves a dish by its ID, including derived allergens and overrides.
   *
   * @param id the dish identifier
   * @return the dish response with computed allergens
   */
  public DishResponse getDish(Long id) {
    Dish dish = dishRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + id));
    return buildDishResponse(dish);
  }

  /**
   * Lists all dishes for a given organization, ordered by name.
   *
   * @param orgId the organization identifier
   * @return list of dish responses
   */
  public List<DishResponse> listDishes(Long orgId) {
    return dishRepository.findByOrganizationIdOrderByNameAsc(orgId).stream()
        .map(this::buildDishResponse)
        .collect(Collectors.toList());
  }

  /**
   * Approves a dish by recording the current user and timestamp.
   *
   * @param id          the dish identifier
   * @param currentUser the authenticated user approving the dish
   * @return the updated dish response
   */
  @Transactional
  public DishResponse approveDish(Long id, AppUser currentUser) {
    Dish dish = dishRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + id));

    dish.setLastApprovedAt(LocalDateTime.now());
    dish.setLastApprovedBy(currentUser);
    dish.setAllergenApprovalValid(true);
    dish = dishRepository.save(dish);

    log.info("Dish approved: {} (id={}) by {}", dish.getName(), dish.getId(),
        currentUser.getUsername());
    return buildDishResponse(dish);
  }

  /**
   * Adds an allergen override to a dish.
   *
   * @param dishId  the dish identifier
   * @param request the override details
   * @return the updated dish response
   */
  @Transactional
  public DishResponse addOverride(Long dishId, DishAllergenOverrideRequest request) {
    Dish dish = dishRepository.findById(dishId)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + dishId));

    Allergen allergen = allergenRepository.findById(request.allergenId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Allergen not found with id: " + request.allergenId()));

    DishAllergenOverride override = new DishAllergenOverride();
    override.setDish(dish);
    override.setAllergen(allergen);
    override.setIncluded(request.included());
    override.setReason(request.reason());
    dishAllergenOverrideRepository.save(override);
    invalidateApproval(dish);
    dishRepository.save(dish);

    log.info("Override added to dish {} (id={}): allergen={}, included={}",
        dish.getName(), dish.getId(), allergen.getCode(), request.included());
    return buildDishResponse(dish);
  }

  /**
   * Removes an allergen override from a dish.
   *
   * @param dishId     the dish identifier
   * @param overrideId the override identifier to remove
   * @return the updated dish response
   */
  @Transactional
  public DishResponse removeOverride(Long dishId, Long overrideId) {
    Dish dish = dishRepository.findById(dishId)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + dishId));

    DishAllergenOverride override = dishAllergenOverrideRepository.findById(overrideId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Override not found with id: " + overrideId));

    if (!override.getDish().getId().equals(dishId)) {
      throw new IllegalArgumentException("Override does not belong to dish: " + dishId);
    }

    dishAllergenOverrideRepository.delete(override);
    invalidateApproval(dish);
    dishRepository.save(dish);
    log.info("Override removed from dish {} (id={}): overrideId={}",
        dish.getName(), dish.getId(), overrideId);
    return buildDishResponse(dish);
  }

  @Transactional
  public void deleteDish(Long id) {
    Dish dish = dishRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Dish not found with id: " + id));
    dishAllergenOverrideRepository.deleteByDishId(id);
    dishIngredientRepository.deleteByDishId(id);
    dishRepository.delete(dish);
    log.info("Dish deleted: id={}", id);
  }

  /**
   * Generates an allergen sheet for all active dishes in an organization.
   *
   * <p>Each entry represents a dish-allergen combination, indicating whether the
   * allergen is present and whether the status was overridden.</p>
   *
   * @param orgId the organization identifier
   * @return list of allergen sheet entries
   */
  public List<AllergenSheetEntry> getAllergenSheet(Long orgId) {
    List<Dish> dishes = dishRepository.findByOrganizationIdAndActiveTrue(orgId);
    List<Allergen> allergens = allergenRepository.findByActiveTrue();
    List<AllergenSheetEntry> entries = new ArrayList<>();

    for (Dish dish : dishes) {
      Set<Long> derivedAllergenIds = computeDerivedAllergenIds(dish);
      List<DishAllergenOverride> overrides = dishAllergenOverrideRepository.findByDishId(
          dish.getId());
      Set<Long> overriddenAllergenIds = overrides.stream()
          .map(o -> o.getAllergen().getId())
          .collect(Collectors.toSet());

      // Apply overrides to get final set
      Set<Long> finalAllergenIds = new HashSet<>(derivedAllergenIds);
      for (DishAllergenOverride override : overrides) {
        if (override.isIncluded()) {
          finalAllergenIds.add(override.getAllergen().getId());
        } else {
          finalAllergenIds.remove(override.getAllergen().getId());
        }
      }

      for (Allergen allergen : allergens) {
        entries.add(new AllergenSheetEntry(
            dish.getName(),
            allergen.getCode(),
            allergen.getNameNo(),
            allergen.getNameEn(),
            finalAllergenIds.contains(allergen.getId()),
            overriddenAllergenIds.contains(allergen.getId())
        ));
      }
    }

    return entries;
  }

  /**
   * Computes the set of allergen IDs derived from the dish's ingredients, before overrides.
   *
   * @param dish the dish entity
   * @return set of allergen IDs from all ingredients
   */
  Set<Long> computeDerivedAllergenIds(Dish dish) {
    List<DishIngredient> dishIngredients = dishIngredientRepository.findByDishId(dish.getId());
    Set<Long> allergenIds = new HashSet<>();

    for (DishIngredient di : dishIngredients) {
      Ingredient ingredient = di.getIngredient();
      for (Allergen allergen : ingredient.getAllergens()) {
        allergenIds.add(allergen.getId());
      }
    }

    return allergenIds;
  }

  /**
   * Computes the final list of allergens for a dish by deriving from ingredients and
   * applying overrides.
   *
   * @param dish the dish entity
   * @return list of final allergen responses
   */
  List<AllergenResponse> computeDerivedAllergens(Dish dish) {
    Set<Long> derivedIds = computeDerivedAllergenIds(dish);
    List<DishAllergenOverride> overrides = dishAllergenOverrideRepository.findByDishId(
        dish.getId());

    Set<Long> finalIds = new HashSet<>(derivedIds);
    for (DishAllergenOverride override : overrides) {
      if (override.isIncluded()) {
        finalIds.add(override.getAllergen().getId());
      } else {
        finalIds.remove(override.getAllergen().getId());
      }
    }

    if (finalIds.isEmpty()) {
      return List.of();
    }

    return allergenRepository.findAllById(finalIds).stream()
        .map(a -> new AllergenResponse(a.getId(), a.getCode(), a.getNameNo(), a.getNameEn()))
        .collect(Collectors.toList());
  }

  /**
   * Saves ingredient links for a dish.
   *
   * @param dish    the dish entity
   * @param entries the list of ingredient entries
   */
  private void saveDishIngredients(Dish dish, List<DishIngredientEntry> entries) {
    for (DishIngredientEntry entry : entries) {
      Ingredient ingredient = ingredientRepository.findById(entry.ingredientId())
          .orElseThrow(() -> new IllegalArgumentException(
              "Ingredient not found with id: " + entry.ingredientId()));

      DishIngredient di = new DishIngredient();
      di.setDish(dish);
      di.setIngredient(ingredient);
      di.setQuantityText(entry.quantityText());
      dishIngredientRepository.save(di);
    }
  }

  /**
   * Builds a full dish response including ingredients, derived allergens, and overrides.
   *
   * @param dish the dish entity
   * @return the complete dish response DTO
   */
  private DishResponse buildDishResponse(Dish dish) {
    List<DishIngredient> dishIngredients = dishIngredientRepository.findByDishId(dish.getId());
    List<DishIngredientResponse> ingredientResponses = dishIngredients.stream()
        .map(di -> new DishIngredientResponse(
            di.getIngredient().getId(),
            di.getIngredient().getName(),
            di.getQuantityText()))
        .collect(Collectors.toList());

    List<AllergenResponse> derivedAllergens = computeDerivedAllergens(dish);

    List<DishAllergenOverride> overrides = dishAllergenOverrideRepository.findByDishId(
        dish.getId());
    List<DishAllergenOverrideResponse> overrideResponses = overrides.stream()
        .map(o -> new DishAllergenOverrideResponse(
            o.getId(),
            new AllergenResponse(
                o.getAllergen().getId(),
                o.getAllergen().getCode(),
                o.getAllergen().getNameNo(),
                o.getAllergen().getNameEn()),
            o.isIncluded(),
            o.getReason()))
        .collect(Collectors.toList());

    boolean changedSinceApproval = dish.getLastApprovedAt() == null || !dish.isAllergenApprovalValid();

    return new DishResponse(
        dish.getId(),
        dish.getName(),
        dish.getDescription(),
        dish.isActive(),
        ingredientResponses,
        derivedAllergens,
        overrideResponses,
        changedSinceApproval,
        dish.getLastApprovedAt(),
        dish.getLastApprovedBy() != null ? dish.getLastApprovedBy().getUsername() : null,
        dish.getNotes(),
        dish.getCreatedAt(),
        dish.getUpdatedAt()
    );
  }

  private void invalidateApproval(Dish dish) {
    dish.setAllergenApprovalValid(false);
  }
}
