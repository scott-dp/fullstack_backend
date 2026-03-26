package stud.ntnu.no.fullstack_project.dto.routine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for reviewing a routine.")
public record ReviewRoutineRequest(
    @Schema(description = "Review notes.")
    @Size(max = 2000)
    String notes
) {}
