package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a bevilling condition.
 */
@Schema(description = "Request payload for updating a bevilling condition.")
public record UpdateConditionRequest(
    @Schema(description = "Updated title.")
    String title,

    @Schema(description = "Updated description.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Whether the condition is active.", example = "true")
    Boolean active
) {}
