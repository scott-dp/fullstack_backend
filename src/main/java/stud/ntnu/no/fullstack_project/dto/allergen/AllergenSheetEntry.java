package stud.ntnu.no.fullstack_project.dto.allergen;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Single entry in the allergen sheet, representing the presence of an allergen in a dish.
 *
 * @param dishName      name of the dish
 * @param allergenCode  allergen code (e.g. GLUTEN)
 * @param allergenNameNo Norwegian name of the allergen
 * @param allergenNameEn English name of the allergen
 * @param present       whether the allergen is present in the dish
 * @param overridden    whether the allergen presence was manually overridden
 */
@Schema(description = "Single entry in the allergen sheet for a dish-allergen combination.")
public record AllergenSheetEntry(
    @Schema(description = "Name of the dish.", example = "Caesar Salad")
    String dishName,

    @Schema(description = "Allergen code.", example = "GLUTEN")
    String allergenCode,

    @Schema(description = "Norwegian name of the allergen.", example = "Gluten")
    String allergenNameNo,

    @Schema(description = "English name of the allergen.", example = "Gluten")
    String allergenNameEn,

    @Schema(description = "Whether the allergen is present in the dish.", example = "true")
    boolean present,

    @Schema(description = "Whether the allergen presence was manually overridden.", example = "false")
    boolean overridden
) {}
