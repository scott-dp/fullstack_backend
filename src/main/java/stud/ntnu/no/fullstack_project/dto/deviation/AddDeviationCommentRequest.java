package stud.ntnu.no.fullstack_project.dto.deviation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for adding a comment to an existing deviation.
 *
 * @param content text content of the comment
 */
@Schema(description = "Request payload for adding a comment to a deviation.")
public record AddDeviationCommentRequest(
    @Schema(description = "Text content of the comment.", example = "Technician has been called to fix the fridge.")
    @NotBlank @Size(max = 2000)
    String content
) {}
