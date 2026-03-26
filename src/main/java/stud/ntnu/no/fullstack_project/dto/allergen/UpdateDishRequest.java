package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for updating an existing dish.
 *
 * @param name          updated dish name
 * @param description   updated description
 * @param notes         updated notes
 * @param ingredientIds updated list of ingredient entries
 */
@Schema(description = "Request payload for updating an existing dish.")
public record UpdateDishRequest(
    @Schema(description = "Updated name of the dish.", example = "Caesar Salad Deluxe")
    @Size(max = 255)
    String name,

    @Schema(description = "Updated description of the dish.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Updated notes about the dish.")
    @Size(max = 2000)
    String notes,

    @Schema(description = "Updated list of ingredient entries for this dish.")
    @Valid
    List<DishIngredientEntry> ingredientIds
) {}
