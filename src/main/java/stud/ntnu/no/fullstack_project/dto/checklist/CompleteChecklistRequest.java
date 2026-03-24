package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request payload for completing a checklist by submitting answers for each item.
 *
 * @param templateId ID of the checklist template being completed
 * @param answers    list of answers, one per checklist item
 * @param comment    optional overall comment for the completion
 */
@Schema(description = "Request payload for completing a checklist template.")
public record CompleteChecklistRequest(
    @Schema(description = "ID of the checklist template being completed.", example = "1")
    @NotNull
    Long templateId,

    @ArraySchema(schema = @Schema(implementation = ChecklistAnswerRequest.class))
    @NotEmpty @Valid
    List<ChecklistAnswerRequest> answers,

    @Schema(description = "Optional overall comment for the completion.", example = "All items checked during morning shift.")
    String comment
) {}
