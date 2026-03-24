package stud.ntnu.no.fullstack_project.dto.deviation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a deviation record with its comments.
 *
 * @param id                 unique deviation identifier
 * @param title              short summary
 * @param description        detailed description
 * @param category           compliance category
 * @param severity           severity level
 * @param status             current status
 * @param reportedByUsername username of the reporter
 * @param assignedToUsername username of the assignee, or {@code null}
 * @param resolvedByUsername username of the resolver, or {@code null}
 * @param resolvedAt         timestamp when resolved, or {@code null}
 * @param createdAt          creation timestamp
 * @param updatedAt          last update timestamp
 * @param comments           list of comments on the deviation
 */
@Schema(description = "Response representing a deviation record with its comments.")
public record DeviationResponse(
    @Schema(description = "Unique deviation identifier.", example = "1")
    Long id,

    @Schema(description = "Short summary of the deviation.", example = "Fridge temperature too high")
    String title,

    @Schema(description = "Detailed description of the deviation.", example = "Walk-in fridge measured at 8C during morning check.")
    String description,

    @Schema(description = "Compliance category.", example = "FOOD_SAFETY")
    String category,

    @Schema(description = "Severity level.", example = "HIGH")
    String severity,

    @Schema(description = "Current deviation status.", example = "OPEN")
    String status,

    @Schema(description = "Username of the user who reported the deviation.", example = "staff")
    String reportedByUsername,

    @Schema(description = "Username of the user the deviation is assigned to.", example = "manager")
    String assignedToUsername,

    @Schema(description = "Username of the user who resolved the deviation.", example = "manager")
    String resolvedByUsername,

    @Schema(description = "Timestamp when the deviation was resolved.", example = "2025-01-16T14:00:00")
    LocalDateTime resolvedAt,

    @Schema(description = "Timestamp when the deviation was created.", example = "2025-01-15T08:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp when the deviation was last updated.", example = "2025-01-15T10:00:00")
    LocalDateTime updatedAt,

    @ArraySchema(schema = @Schema(implementation = DeviationCommentResponse.class))
    List<DeviationCommentResponse> comments
) {}
