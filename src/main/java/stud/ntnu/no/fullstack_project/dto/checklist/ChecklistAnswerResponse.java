package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing an answer to a single checklist item.
 *
 * @param id              unique answer identifier
 * @param itemId          ID of the checklist item that was answered
 * @param itemDescription description text of the checklist item
 * @param checked         whether the item was marked as checked
 * @param comment         optional comment provided with the answer
 */
@Schema(description = "Answer to a single checklist item within a completion.")
public record ChecklistAnswerResponse(
    @Schema(description = "Unique answer identifier.", example = "1")
    Long id,

    @Schema(description = "ID of the checklist item that was answered.", example = "1")
    Long itemId,

    @Schema(description = "Description text of the checklist item.", example = "Verify fridge temperature is below 4C")
    String itemDescription,

    @Schema(description = "Whether the item was marked as checked.", example = "true")
    boolean checked,

    @Schema(description = "Optional comment provided with the answer.", example = "Temperature was 3.5C")
    String comment
) {}
