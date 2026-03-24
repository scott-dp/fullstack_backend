package stud.ntnu.no.fullstack_project.dto.deviation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a single comment on a deviation.
 *
 * @param id             unique comment identifier
 * @param authorUsername  username of the comment author
 * @param content        text content of the comment
 * @param createdAt      timestamp when the comment was created
 */
@Schema(description = "Single comment on a deviation.")
public record DeviationCommentResponse(
    @Schema(description = "Unique comment identifier.", example = "1")
    Long id,

    @Schema(description = "Username of the comment author.", example = "manager")
    String authorUsername,

    @Schema(description = "Text content of the comment.", example = "Technician has been called to fix the fridge.")
    String content,

    @Schema(description = "Timestamp when the comment was created.", example = "2025-01-15T09:00:00")
    LocalDateTime createdAt
) {}
