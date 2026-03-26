package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing an ingredient within a dish.
 *
 * @param ingredientId   the ingredient identifier
 * @param ingredientName the ingredient name
 * @param quantityText   optional quantity description
 */
@Schema(description = "Response representing an ingredient entry in a dish.")
public record DishIngredientResponse(
    @Schema(description = "Ingredient identifier.", example = "1")
    Long ingredientId,

    @Schema(description = "Ingredient name.", example = "Parmesan")
    String ingredientName,

    @Schema(description = "Optional quantity description.", example = "200g")
    String quantityText
) {}
