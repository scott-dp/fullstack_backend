package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for creating a new ingredient.
 *
 * @param name        ingredient name
 * @param notes       optional notes about the ingredient
 * @param allergenIds list of allergen IDs associated with this ingredient
 */
@Schema(description = "Request payload for creating a new ingredient.")
public record CreateIngredientRequest(
    @Schema(description = "Name of the ingredient.", example = "Parmesan")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Optional notes about the ingredient.", example = "Aged 24 months")
    @Size(max = 2000)
    String notes,

    @Schema(description = "List of allergen IDs associated with this ingredient.")
    List<Long> allergenIds
) {}
