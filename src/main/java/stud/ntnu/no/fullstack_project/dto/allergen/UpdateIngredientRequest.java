package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for updating an existing ingredient.
 *
 * @param name        updated ingredient name
 * @param notes       updated notes
 * @param allergenIds updated list of allergen IDs
 */
@Schema(description = "Request payload for updating an existing ingredient.")
public record UpdateIngredientRequest(
    @Schema(description = "Updated name of the ingredient.", example = "Parmesan Reggiano")
    @Size(max = 255)
    String name,

    @Schema(description = "Updated notes about the ingredient.")
    @Size(max = 2000)
    String notes,

    @Schema(description = "Updated list of allergen IDs associated with this ingredient.")
    List<Long> allergenIds
) {}
