package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing an ingredient with its allergens.
 *
 * @param id        unique ingredient identifier
 * @param name      ingredient name
 * @param notes     optional notes
 * @param allergens list of allergens associated with this ingredient
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
@Schema(description = "Response representing an ingredient with associated allergens.")
public record IngredientResponse(
    @Schema(description = "Unique ingredient identifier.", example = "1")
    Long id,

    @Schema(description = "Ingredient name.", example = "Parmesan")
    String name,

    @Schema(description = "Optional notes about the ingredient.")
    String notes,

    @Schema(description = "List of allergens associated with this ingredient.")
    List<AllergenResponse> allergens,

    @Schema(description = "Timestamp when the ingredient was created.", example = "2025-01-15T08:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp when the ingredient was last updated.", example = "2025-01-15T10:00:00")
    LocalDateTime updatedAt
) {}
