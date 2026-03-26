package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a training assignment.
 *
 * @param id                 unique assignment identifier
 * @param templateId         training template identifier
 * @param templateTitle      training template title
 * @param assigneeUsername   username of the assigned user
 * @param assignedByUsername username of the assigning user
 * @param assignedAt         timestamp when assigned
 * @param dueAt              due date, or {@code null}
 * @param status             current assignment status
 */
@Schema(description = "Response representing a training assignment.")
public record TrainingAssignmentResponse(
    @Schema(description = "Unique assignment identifier.", example = "1")
    Long id,

    @Schema(description = "Training template identifier.", example = "1")
    Long templateId,

    @Schema(description = "Training template title.", example = "Basic food hygiene")
    String templateTitle,

    @Schema(description = "Username of the assigned user.", example = "staff")
    String assigneeUsername,

    @Schema(description = "Username of the assigning user.", example = "manager")
    String assignedByUsername,

    @Schema(description = "Timestamp when assigned.", example = "2025-01-15T08:30:00")
    LocalDateTime assignedAt,

    @Schema(description = "Due date.", example = "2025-06-01T00:00:00")
    LocalDateTime dueAt,

    @Schema(description = "Current assignment status.", example = "ASSIGNED")
    String status
) {}
