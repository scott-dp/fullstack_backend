package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for completing a training assignment.
 *
 * @param acknowledgementChecked whether the user acknowledged the training content
 * @param comments               optional comments from the user
 */
@Schema(description = "Request payload for completing a training assignment.")
public record CompleteTrainingRequest(
    @Schema(description = "Whether the user acknowledged the training content.", example = "true")
    boolean acknowledgementChecked,

    @Schema(description = "Optional comments from the user.", example = "Understood all material.")
    String comments
) {}
