package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a completed checklist instance with its answers.
 *
 * @param id                  unique completion identifier
 * @param templateId          ID of the completed template
 * @param templateTitle       title of the completed template
 * @param completedByUsername username of the user who completed it
 * @param completedAt         timestamp of completion
 * @param status              completion status (COMPLETE or INCOMPLETE)
 * @param comment             optional overall comment
 * @param answers             list of individual item answers
 */
@Schema(description = "Response representing a completed checklist instance with its answers.")
public record ChecklistCompletionResponse(
    @Schema(description = "Unique completion identifier.", example = "1")
    Long id,

    @Schema(description = "ID of the completed template.", example = "1")
    Long templateId,

    @Schema(description = "Title of the completed template.", example = "Morning Kitchen Checklist")
    String templateTitle,

    @Schema(description = "Username of the user who completed the checklist.", example = "staff")
    String completedByUsername,

    @Schema(description = "Timestamp of completion.", example = "2025-01-15T08:30:00")
    LocalDateTime completedAt,

    @Schema(description = "Completion status: COMPLETE if all items checked, INCOMPLETE otherwise.", example = "COMPLETE")
    String status,

    @Schema(description = "Optional overall comment.", example = "All items checked during morning shift.")
    String comment,

    @ArraySchema(schema = @Schema(implementation = ChecklistAnswerResponse.class))
    List<ChecklistAnswerResponse> answers
) {}
