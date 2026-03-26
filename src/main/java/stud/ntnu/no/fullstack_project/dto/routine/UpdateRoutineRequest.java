package stud.ntnu.no.fullstack_project.dto.routine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating a routine.")
public record UpdateRoutineRequest(
    @Size(max = 255)
    String name,

    String moduleType,

    String category,

    @Size(max = 2000)
    String description,

    @Size(max = 1000)
    String purpose,

    String responsibleRole,

    String frequencyType,

    @Size(max = 4000)
    String stepsText,

    @Size(max = 2000)
    String whatIsDeviationText,

    @Size(max = 2000)
    String correctiveActionText,

    @Size(max = 2000)
    String requiredEvidenceText,

    Long linkedChecklistTemplateId,

    Integer reviewIntervalDays
) {}
