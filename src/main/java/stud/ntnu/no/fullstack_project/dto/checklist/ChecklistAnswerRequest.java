package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload representing an answer to a single checklist item.
 *
 * @param itemId  ID of the checklist item being answered
 * @param checked whether the item was marked as checked / passing
 * @param comment optional comment for the answer
 */
@Schema(description = "Answer to a single checklist item.")
public record ChecklistAnswerRequest(
    @Schema(description = "ID of the checklist item being answered.", example = "1")
    @NotNull
    Long itemId,

    @Schema(description = "Whether the item was marked as checked.", example = "true")
    boolean checked,

    @Schema(description = "Optional comment for the answer.", example = "Temperature was 3.5C")
    String comment
) {}
