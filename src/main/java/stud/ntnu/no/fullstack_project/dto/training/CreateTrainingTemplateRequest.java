package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new training template.
 *
 * @param title                  short title for the training
 * @param moduleType             compliance module (IK_MAT, IK_ALKOHOL, SHARED)
 * @param category               training category
 * @param description            detailed description
 * @param contentText            full training content text
 * @param requiredForRole        role required to complete this training
 * @param isMandatory            whether the training is mandatory
 * @param validityDays           number of days the training remains valid
 * @param acknowledgmentRequired whether acknowledgment is required on completion
 * @param linkedRoutineId        optional linked routine identifier
 */
@Schema(description = "Request payload for creating a new training template.")
public record CreateTrainingTemplateRequest(
    @Schema(description = "Short title for the training.", example = "Basic food hygiene")
    @NotBlank @Size(max = 255)
    String title,

    @Schema(description = "Compliance module type.", example = "IK_MAT")
    @NotNull
    String moduleType,

    @Schema(description = "Training category.", example = "FOOD_HYGIENE")
    @NotNull
    String category,

    @Schema(description = "Detailed description of the training.", example = "Covers basic food hygiene principles.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Full training content text.", example = "Training content goes here...")
    @Size(max = 4000)
    String contentText,

    @Schema(description = "Role required to complete this training.", example = "ALL")
    @NotNull
    String requiredForRole,

    @Schema(description = "Whether the training is mandatory.", example = "true")
    boolean isMandatory,

    @Schema(description = "Number of days the training remains valid.", example = "365")
    Integer validityDays,

    @Schema(description = "Whether acknowledgment is required on completion.", example = "true")
    boolean acknowledgmentRequired,

    @Schema(description = "Optional linked routine identifier.", example = "1")
    Long linkedRoutineId
) {}
