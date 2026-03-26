package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request payload for assigning a training template to one or more users.
 *
 * @param assigneeUserIds list of user IDs to assign the training to
 * @param dueAt           optional due date in ISO-8601 format
 */
@Schema(description = "Request payload for assigning a training template to users.")
public record AssignTrainingRequest(
    @Schema(description = "List of user IDs to assign the training to.", example = "[2, 3]")
    @NotNull
    List<Long> assigneeUserIds,

    @Schema(description = "Optional due date in ISO-8601 format.", example = "2025-06-01T00:00:00")
    String dueAt
) {}
