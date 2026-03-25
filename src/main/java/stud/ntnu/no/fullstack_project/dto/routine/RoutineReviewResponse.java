package stud.ntnu.no.fullstack_project.dto.routine;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response representing a routine review record.")
public record RoutineReviewResponse(
    Long id,
    Long routineId,
    String reviewedByUsername,
    LocalDateTime reviewedAt,
    String notes,
    LocalDateTime nextReviewAt
) {}
