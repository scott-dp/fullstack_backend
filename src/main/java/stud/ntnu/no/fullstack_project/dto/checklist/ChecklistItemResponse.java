package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing a single item within a checklist template.
 *
 * @param id              unique item identifier
 * @param description     text describing what should be checked
 * @param sortOrder       display order within the template
 * @param requiresComment whether a comment is mandatory when completing this item
 */
@Schema(description = "Single item within a checklist template.")
public record ChecklistItemResponse(
    @Schema(description = "Unique item identifier.", example = "1")
    Long id,

    @Schema(description = "Text describing what should be checked.", example = "Verify fridge temperature is below 4C")
    String description,

    @Schema(description = "Display order within the template.", example = "1")
    int sortOrder,

    @Schema(description = "Whether a comment is mandatory when completing this item.", example = "false")
    boolean requiresComment
) {}
