package stud.ntnu.no.fullstack_project.dto.deviation;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for updating an existing deviation's status or assignment.
 *
 * <p>Both fields are optional; only non-null values will be applied.</p>
 *
 * @param status       new status (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED), or {@code null}
 * @param assignedToId ID of the user to assign, or {@code null}
 */
@Schema(description = "Request payload for updating a deviation's status or assignment.")
public record UpdateDeviationRequest(
    @Schema(description = "New deviation status.", example = "IN_PROGRESS")
    String status,

    @Schema(description = "ID of the user to assign the deviation to.", example = "2")
    Long assignedToId
) {}
