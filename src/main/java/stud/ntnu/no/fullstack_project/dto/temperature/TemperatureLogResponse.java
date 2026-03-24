package stud.ntnu.no.fullstack_project.dto.temperature;

import java.time.LocalDateTime;

public record TemperatureLogResponse(
    Long id,
    String location,
    double temperature,
    double minThreshold,
    double maxThreshold,
    String status,
    String recordedByUsername,
    LocalDateTime recordedAt,
    String comment
) {}
