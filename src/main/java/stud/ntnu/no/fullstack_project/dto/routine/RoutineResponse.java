package stud.ntnu.no.fullstack_project.dto.routine;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response representing a routine definition.")
public record RoutineResponse(
    Long id,
    String name,
    String moduleType,
    String category,
    String description,
    String purpose,
    String responsibleRole,
    String frequencyType,
    String stepsText,
    String whatIsDeviationText,
    String correctiveActionText,
    String requiredEvidenceText,
    Long linkedChecklistTemplateId,
    String linkedChecklistTemplateName,
    boolean active,
    Integer reviewIntervalDays,
    LocalDateTime lastReviewedAt,
    int versionNumber,
    String createdByUsername,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
