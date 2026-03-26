package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Nested record representing an ingredient entry in a dish request.
 *
 * @param ingredientId the ingredient identifier
 * @param quantityText optional quantity description
 */
@Schema(description = "An ingredient entry for a dish with optional quantity text.")
public record DishIngredientEntry(
    @Schema(description = "Ingredient identifier.", example = "1")
    @NotNull
    Long ingredientId,

    @Schema(description = "Optional quantity description.", example = "200g")
    String quantityText
) {}
