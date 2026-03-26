package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for adding an allergen override to a dish.
 *
 * @param allergenId the allergen to override
 * @param included   whether the allergen should be forcibly included or excluded
 * @param reason     reason for the override
 */
@Schema(description = "Request payload for adding an allergen override to a dish.")
public record DishAllergenOverrideRequest(
    @Schema(description = "Allergen identifier to override.", example = "1")
    @NotNull
    Long allergenId,

    @Schema(description = "Whether the allergen should be forcibly included (true) or excluded (false).", example = "false")
    boolean included,

    @Schema(description = "Reason for the override.", example = "Cross-contamination risk eliminated by supplier change.")
    @NotBlank @Size(max = 500)
    String reason
) {}
