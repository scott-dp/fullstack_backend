package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing training template.
 *
 * <p>All fields are optional; only non-null values will be applied.</p>
 *
 * @param title                  updated title
 * @param moduleType             updated module type
 * @param category               updated category
 * @param description            updated description
 * @param contentText            updated content text
 * @param requiredForRole        updated required role
 * @param isMandatory            updated mandatory flag
 * @param validityDays           updated validity period in days
 * @param acknowledgmentRequired updated acknowledgment requirement
 * @param active                 updated active status
 */
@Schema(description = "Request payload for updating an existing training template.")
public record UpdateTrainingTemplateRequest(
    @Schema(description = "Updated title.", example = "Advanced food hygiene")
    @Size(max = 255)
    String title,

    @Schema(description = "Updated module type.", example = "IK_MAT")
    String moduleType,

    @Schema(description = "Updated training category.", example = "FOOD_HYGIENE")
    String category,

    @Schema(description = "Updated description.", example = "Updated description.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Updated content text.", example = "Updated content.")
    @Size(max = 4000)
    String contentText,

    @Schema(description = "Updated required role.", example = "STAFF")
    String requiredForRole,

    @Schema(description = "Updated mandatory flag.", example = "false")
    Boolean isMandatory,

    @Schema(description = "Updated validity period in days.", example = "180")
    Integer validityDays,

    @Schema(description = "Updated acknowledgment requirement.", example = "false")
    Boolean acknowledgmentRequired,

    @Schema(description = "Updated active status.", example = "true")
    Boolean active
) {}
