package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a training completion record.
 *
 * @param id                     unique completion identifier
 * @param assignmentId           training assignment identifier
 * @param completedByUsername    username of the user who completed the training
 * @param completedAt            completion timestamp
 * @param acknowledgementChecked whether acknowledgment was checked
 * @param comments               optional comments
 * @param expiresAt              expiry timestamp, or {@code null}
 */
@Schema(description = "Response representing a training completion record.")
public record TrainingCompletionResponse(
    @Schema(description = "Unique completion identifier.", example = "1")
    Long id,

    @Schema(description = "Training assignment identifier.", example = "1")
    Long assignmentId,

    @Schema(description = "Username of the user who completed the training.", example = "staff")
    String completedByUsername,

    @Schema(description = "Completion timestamp.", example = "2025-01-16T14:00:00")
    LocalDateTime completedAt,

    @Schema(description = "Whether acknowledgment was checked.", example = "true")
    boolean acknowledgementChecked,

    @Schema(description = "Optional comments.", example = "Understood all material.")
    String comments,

    @Schema(description = "Expiry timestamp.", example = "2026-01-16T14:00:00")
    LocalDateTime expiresAt
) {}
