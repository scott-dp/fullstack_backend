package stud.ntnu.no.fullstack_project.dto.temperature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTemperatureLogRequest(
    @NotBlank @Size(max = 255) String location,
    double temperature,
    double minThreshold,
    double maxThreshold,
    @Size(max = 500) String comment
) {}
