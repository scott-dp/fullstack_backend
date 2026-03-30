package stud.ntnu.no.fullstack_project.controller.food;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenSheetEntry;
import stud.ntnu.no.fullstack_project.dto.allergen.CreateDishRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishAllergenOverrideRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.DishResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.UpdateDishRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.DishService;

/**
 * REST controller for dish management, allergen derivation, and overrides.
 *
 * <p>Provides endpoints for creating, listing, updating, approving dishes,
 * managing allergen overrides, and generating the allergen sheet.</p>
 */
@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dishes", description = "Endpoints for dish management and allergen tracking")
public class DishController {

  private final DishService dishService;

  @GetMapping
  @Operation(
      summary = "List dishes for the current user's organization",
      description = "Returns all dishes ordered by name."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dishes retrieved successfully")
  })
  public ResponseEntity<List<DishResponse>> listDishes(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing dishes for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        dishService.listDishes(currentUser.getOrganization().getId())
    );
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a new dish",
      description = "Creates a dish with ingredient links. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Dish created successfully",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> createDish(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Dish details to create.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateDishRequest.class),
              examples = @ExampleObject(name = "Create dish", value = """
                  {
                    "name": "Caesar Salad",
                    "description": "Classic caesar salad with parmesan",
                    "ingredientIds": [
                      {"ingredientId": 1, "quantityText": "50g"},
                      {"ingredientId": 2, "quantityText": "100g"}
                    ]
                  }
                  """)))
      @Valid @RequestBody CreateDishRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating dish name={} by user={}", request.name(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dishService.createDish(request, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a dish by ID",
      description = "Returns a single dish with derived allergens and overrides."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dish found",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dish not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> getDish(@PathVariable Long id) {
    log.info("Fetching dish id={}", id);
    return ResponseEntity.ok(dishService.getDish(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update a dish",
      description = "Updates a dish's name, description, notes, and ingredients. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dish updated successfully",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or dish not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> updateDish(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Fields to update on the dish.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateDishRequest.class),
              examples = @ExampleObject(name = "Update dish", value = """
                  {
                    "name": "Caesar Salad Deluxe",
                    "ingredientIds": [
                      {"ingredientId": 1, "quantityText": "75g"}
                    ]
                  }
                  """)))
      @Valid @RequestBody UpdateDishRequest request
  ) {
    log.info("Updating dish id={}", id);
    return ResponseEntity.ok(dishService.updateDish(id, request));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Approve a dish",
      description = "Records the approval timestamp and the approving user. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dish approved successfully",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dish not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> approveDish(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Approving dish id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.ok(dishService.approveDish(id, currentUser));
  }

  @PostMapping("/{id}/overrides")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Add an allergen override to a dish",
      description = "Adds a manual allergen override (include or exclude) with a reason. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Override added successfully",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or dish/allergen not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> addOverride(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Allergen override details.",
          required = true,
          content = @Content(schema = @Schema(implementation = DishAllergenOverrideRequest.class),
              examples = @ExampleObject(name = "Add override", value = """
                  {
                    "allergenId": 1,
                    "included": false,
                    "reason": "Gluten-free flour substitute used."
                  }
                  """)))
      @Valid @RequestBody DishAllergenOverrideRequest request
  ) {
    log.info("Adding override to dish id={}, allergenId={}", id, request.allergenId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dishService.addOverride(id, request));
  }

  @DeleteMapping("/{id}/overrides/{overrideId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Remove an allergen override from a dish",
      description = "Deletes a specific allergen override. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Override removed successfully",
          content = @Content(schema = @Schema(implementation = DishResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dish or override not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DishResponse> removeOverride(
      @PathVariable Long id,
      @PathVariable Long overrideId
  ) {
    log.info("Removing override overrideId={} from dish id={}", overrideId, id);
    return ResponseEntity.ok(dishService.removeOverride(id, overrideId));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Delete a dish",
      description = "Deletes a dish and its ingredient links and allergen overrides."
  )
  public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
    log.info("Deleting dish id={}", id);
    dishService.deleteDish(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/allergen-sheet")
  @Operation(
      summary = "Get the allergen sheet for the current user's organization",
      description = "Returns a matrix of dish-allergen combinations for all active dishes."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Allergen sheet retrieved successfully")
  })
  public ResponseEntity<List<AllergenSheetEntry>> getAllergenSheet(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Generating allergen sheet for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        dishService.getAllergenSheet(currentUser.getOrganization().getId())
    );
  }
}
