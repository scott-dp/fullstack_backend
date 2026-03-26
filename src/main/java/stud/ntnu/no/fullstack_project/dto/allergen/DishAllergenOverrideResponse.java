package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing an allergen override on a dish.
 *
 * @param id       override identifier
 * @param allergen the overridden allergen
 * @param included whether the allergen is forcibly included or excluded
 * @param reason   reason for the override
 */
@Schema(description = "Response representing an allergen override on a dish.")
public record DishAllergenOverrideResponse(
    @Schema(description = "Override identifier.", example = "1")
    Long id,

    @Schema(description = "The overridden allergen.")
    AllergenResponse allergen,

    @Schema(description = "Whether the allergen is forcibly included (true) or excluded (false).", example = "false")
    boolean included,

    @Schema(description = "Reason for the override.", example = "Cross-contamination risk eliminated by supplier change.")
    String reason
) {}
