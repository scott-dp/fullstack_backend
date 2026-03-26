package stud.ntnu.no.fullstack_project.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing alcohol incident.
 *
 * <p>All fields are optional; only non-null values will be applied.</p>
 *
 * @param severity             new severity level, or {@code null}
 * @param description          new description, or {@code null}
 * @param immediateActionTaken new immediate action text, or {@code null}
 * @param followUpRequired     new follow-up flag, or {@code null}
 * @param status               new status (OPEN, UNDER_REVIEW, CLOSED), or {@code null}
 * @param assignedToId         user ID to assign, or {@code null}
 */
@Schema(description = "Request payload for updating an alcohol incident.")
public record UpdateAlcoholIncidentRequest(
    @Schema(description = "New severity level.", example = "HIGH")
    String severity,

    @Schema(description = "Updated description.", example = "Updated details about the incident.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Updated immediate action taken.", example = "Called police.")
    @Size(max = 2000)
    String immediateActionTaken,

    @Schema(description = "Whether follow-up is required.", example = "true")
    Boolean followUpRequired,

    @Schema(description = "New incident status.", example = "UNDER_REVIEW")
    String status,

    @Schema(description = "User ID to assign the incident to.", example = "2")
    Long assignedToId
) {}
