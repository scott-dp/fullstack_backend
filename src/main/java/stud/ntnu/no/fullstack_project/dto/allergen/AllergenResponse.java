package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing an allergen.
 *
 * @param id     unique allergen identifier
 * @param code   allergen code (e.g. GLUTEN, MILK)
 * @param nameNo Norwegian name
 * @param nameEn English name
 */
@Schema(description = "Response representing an EU allergen.")
public record AllergenResponse(
    @Schema(description = "Unique allergen identifier.", example = "1")
    Long id,

    @Schema(description = "Allergen code.", example = "GLUTEN")
    String code,

    @Schema(description = "Norwegian name of the allergen.", example = "Gluten")
    String nameNo,

    @Schema(description = "English name of the allergen.", example = "Gluten")
    String nameEn
) {}
