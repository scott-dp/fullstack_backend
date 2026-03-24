package stud.ntnu.no.fullstack_project.dto.deviation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDeviationRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 2000) String description,
    @NotNull String category,
    @NotNull String severity
) {}
