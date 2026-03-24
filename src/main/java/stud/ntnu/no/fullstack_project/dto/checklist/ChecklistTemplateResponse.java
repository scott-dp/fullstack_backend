package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a checklist template with its items.
 *
 * @param id                unique template identifier
 * @param title             display title
 * @param description       optional longer description
 * @param frequency         how often the checklist should be completed
 * @param category          compliance category
 * @param active            whether the template is active (not soft-deleted)
 * @param items             ordered list of checklist items
 * @param createdByUsername username of the user who created the template
 * @param createdAt         timestamp when the template was created
 */
@Schema(description = "Response payload representing a checklist template with its items.")
public record ChecklistTemplateResponse(
    @Schema(description = "Unique template identifier.", example = "1")
    Long id,

    @Schema(description = "Display title of the checklist template.", example = "Morning Kitchen Checklist")
    String title,

    @Schema(description = "Optional longer description of the checklist.", example = "Daily morning kitchen opening procedures.")
    String description,

    @Schema(description = "How often the checklist should be completed.", example = "DAILY")
    String frequency,

    @Schema(description = "Compliance category the checklist belongs to.", example = "FOOD_SAFETY")
    String category,

    @Schema(description = "Whether the template is active (not soft-deleted).", example = "true")
    boolean active,

    @ArraySchema(schema = @Schema(implementation = ChecklistItemResponse.class))
    List<ChecklistItemResponse> items,

    @Schema(description = "Username of the user who created the template.", example = "admin")
    String createdByUsername,

    @Schema(description = "Timestamp when the template was created.", example = "2025-01-15T10:30:00")
    LocalDateTime createdAt
) {}
