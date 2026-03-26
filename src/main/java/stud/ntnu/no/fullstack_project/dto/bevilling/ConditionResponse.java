package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing a bevilling condition.
 */
@Schema(description = "Response representing a bevilling condition.")
public record ConditionResponse(
    @Schema(description = "Unique condition identifier.", example = "1")
    Long id,

    @Schema(description = "Condition type.", example = "FOOD_REQUIREMENT")
    String conditionType,

    @Schema(description = "Condition title.", example = "Food must be available")
    String title,

    @Schema(description = "Detailed description.")
    String description,

    @Schema(description = "Whether the condition is currently active.", example = "true")
    boolean active
) {}
