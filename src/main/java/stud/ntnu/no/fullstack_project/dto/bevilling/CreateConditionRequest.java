package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for adding a condition to a bevilling.
 */
@Schema(description = "Request payload for adding a bevilling condition.")
public record CreateConditionRequest(
    @Schema(description = "Condition type.", example = "FOOD_REQUIREMENT")
    @NotNull
    String conditionType,

    @Schema(description = "Condition title.", example = "Food must be available")
    @NotBlank
    String title,

    @Schema(description = "Detailed description.", example = "Hot food must be available during all serving hours.")
    @Size(max = 2000)
    String description
) {}
