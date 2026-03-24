package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a single item within a checklist template.
 *
 * @param description     text describing what should be checked
 * @param sortOrder       display order of the item within the checklist
 * @param requiresComment whether a comment is mandatory when completing this item
 */
@Schema(description = "Single item within a checklist template creation request.")
public record CreateChecklistItemRequest(
    @Schema(description = "Text describing what should be checked.", example = "Verify fridge temperature is below 4C")
    @NotBlank @Size(max = 500)
    String description,

    @Schema(description = "Display order of the item within the checklist.", example = "1")
    int sortOrder,

    @Schema(description = "Whether a comment is mandatory when completing this item.", example = "false")
    boolean requiresComment
) {}
