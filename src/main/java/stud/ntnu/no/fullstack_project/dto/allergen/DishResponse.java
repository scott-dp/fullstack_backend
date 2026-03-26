package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a dish with derived allergens and overrides.
 *
 * @param id                      unique dish identifier
 * @param name                    dish name
 * @param description             optional description
 * @param active                  whether the dish is active
 * @param ingredients             list of ingredients in this dish
 * @param derivedAllergens        allergens derived from ingredients after overrides
 * @param overrides               allergen overrides applied to this dish
 * @param changedSinceApproval    whether the dish has been modified since last approval
 * @param lastApprovedAt          timestamp of last approval
 * @param lastApprovedByUsername  username of the user who last approved the dish
 * @param notes                   optional notes
 * @param createdAt               creation timestamp
 * @param updatedAt               last update timestamp
 */
@Schema(description = "Response representing a dish with derived allergens and overrides.")
public record DishResponse(
    @Schema(description = "Unique dish identifier.", example = "1")
    Long id,

    @Schema(description = "Dish name.", example = "Caesar Salad")
    String name,

    @Schema(description = "Optional description of the dish.")
    String description,

    @Schema(description = "Whether the dish is currently active.", example = "true")
    boolean active,

    @Schema(description = "Ingredients used in this dish.")
    List<DishIngredientResponse> ingredients,

    @Schema(description = "Allergens derived from ingredients with overrides applied.")
    List<AllergenResponse> derivedAllergens,

    @Schema(description = "Allergen overrides applied to this dish.")
    List<DishAllergenOverrideResponse> overrides,

    @Schema(description = "Whether the dish has changed since the last approval.", example = "false")
    boolean changedSinceApproval,

    @Schema(description = "Timestamp of the last approval.", example = "2025-01-15T10:00:00")
    LocalDateTime lastApprovedAt,

    @Schema(description = "Username of the user who last approved this dish.", example = "admin")
    String lastApprovedByUsername,

    @Schema(description = "Optional notes about the dish.")
    String notes,

    @Schema(description = "Timestamp when the dish was created.", example = "2025-01-15T08:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp when the dish was last updated.", example = "2025-01-15T10:00:00")
    LocalDateTime updatedAt
) {}
