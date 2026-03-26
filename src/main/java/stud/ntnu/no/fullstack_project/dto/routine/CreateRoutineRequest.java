package stud.ntnu.no.fullstack_project.dto.routine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a new routine.")
public record CreateRoutineRequest(
    @Schema(description = "Name of the routine.", example = "Morning fridge temperature control")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Module type: IK_MAT, IK_ALKOHOL, or SHARED.", example = "IK_MAT")
    @NotNull
    String moduleType,

    @Schema(description = "Routine category.", example = "TEMPERATURE")
    @NotNull
    String category,

    @Schema(description = "Description of the routine.", example = "Check fridge temperatures every morning before service.")
    @Size(max = 2000)
    String description,

    @Schema(description = "Purpose of this routine.", example = "Ensure food is stored at safe temperatures.")
    @Size(max = 1000)
    String purpose,

    @Schema(description = "Responsible role.", example = "STAFF")
    @NotNull
    String responsibleRole,

    @Schema(description = "Frequency type.", example = "DAILY")
    @NotNull
    String frequencyType,

    @Schema(description = "Steps to perform.")
    @Size(max = 4000)
    String stepsText,

    @Schema(description = "What counts as a deviation.")
    @Size(max = 2000)
    String whatIsDeviationText,

    @Schema(description = "Corrective action to take.")
    @Size(max = 2000)
    String correctiveActionText,

    @Schema(description = "Required evidence.")
    @Size(max = 2000)
    String requiredEvidenceText,

    @Schema(description = "Optional linked checklist template ID.")
    Long linkedChecklistTemplateId,

    @Schema(description = "Review interval in days.")
    Integer reviewIntervalDays
) {}
