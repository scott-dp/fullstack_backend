package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a training template.
 *
 * @param id                     unique template identifier
 * @param organizationId         owning organization identifier
 * @param title                  training title
 * @param moduleType             compliance module type
 * @param description            training description
 * @param contentText            full training content text
 * @param category               training category
 * @param requiredForRole        role required to complete
 * @param isMandatory            whether the training is mandatory
 * @param validityDays           number of days the training remains valid
 * @param acknowledgmentRequired whether acknowledgment is required
 * @param active                 whether the template is active
 * @param createdAt              creation timestamp
 * @param updatedAt              last update timestamp
 */
@Schema(description = "Response representing a training template.")
public record TrainingTemplateResponse(
    @Schema(description = "Unique template identifier.", example = "1")
    Long id,

    @Schema(description = "Owning organization identifier.", example = "1")
    Long organizationId,

    @Schema(description = "Training title.", example = "Basic food hygiene")
    String title,

    @Schema(description = "Compliance module type.", example = "IK_MAT")
    String moduleType,

    @Schema(description = "Training description.", example = "Covers basic food hygiene principles.")
    String description,

    @Schema(description = "Full training content text.", example = "Training content goes here...")
    String contentText,

    @Schema(description = "Training category.", example = "FOOD_HYGIENE")
    String category,

    @Schema(description = "Role required to complete.", example = "ALL")
    String requiredForRole,

    @Schema(description = "Whether the training is mandatory.", example = "true")
    boolean isMandatory,

    @Schema(description = "Number of days the training remains valid.", example = "365")
    Integer validityDays,

    @Schema(description = "Whether acknowledgment is required.", example = "true")
    boolean acknowledgmentRequired,

    @Schema(description = "Whether the template is active.", example = "true")
    boolean active,

    @Schema(description = "Creation timestamp.", example = "2025-01-15T08:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp.", example = "2025-01-15T10:00:00")
    LocalDateTime updatedAt
) {}
