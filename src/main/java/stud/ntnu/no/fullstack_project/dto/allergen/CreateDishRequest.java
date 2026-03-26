package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for creating a new dish.
 *
 * @param name          dish name
 * @param description   optional description
 * @param notes         optional notes
 * @param ingredientIds list of ingredient entries with optional quantity text
 */
@Schema(description = "Request payload for creating a new dish.")
public record CreateDishRequest(
    @Schema(description = "Name of the dish.", example = "Caesar Salad")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Optional description of the dish.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Optional notes about the dish.")
    @Size(max = 2000)
    String notes,

    @Schema(description = "List of ingredient entries for this dish.")
    @Valid
    List<DishIngredientEntry> ingredientIds
) {}
