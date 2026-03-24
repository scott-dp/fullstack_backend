package stud.ntnu.no.fullstack_project.dto.temperature;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a recorded temperature measurement.
 *
 * @param id                 unique log identifier
 * @param location           name of the measurement location
 * @param temperature        measured temperature value
 * @param minThreshold       minimum acceptable threshold
 * @param maxThreshold       maximum acceptable threshold
 * @param status             computed status (NORMAL, WARNING, or CRITICAL)
 * @param recordedByUsername username of the user who recorded the measurement
 * @param recordedAt         timestamp of the measurement
 * @param comment            optional comment
 */
@Schema(description = "Response representing a recorded temperature measurement.")
public record TemperatureLogResponse(
    @Schema(description = "Unique log identifier.", example = "1")
    Long id,

    @Schema(description = "Name of the measurement location.", example = "Walk-in Fridge")
    String location,

    @Schema(description = "Measured temperature value in degrees Celsius.", example = "3.5")
    double temperature,

    @Schema(description = "Minimum acceptable temperature threshold in degrees Celsius.", example = "0.0")
    double minThreshold,

    @Schema(description = "Maximum acceptable temperature threshold in degrees Celsius.", example = "4.0")
    double maxThreshold,

    @Schema(description = "Computed status based on thresholds: NORMAL, WARNING, or CRITICAL.", example = "NORMAL")
    String status,

    @Schema(description = "Username of the user who recorded the measurement.", example = "staff")
    String recordedByUsername,

    @Schema(description = "Timestamp when the measurement was recorded.", example = "2025-01-15T08:30:00")
    LocalDateTime recordedAt,

    @Schema(description = "Optional comment about the measurement.", example = "Measured after restocking.")
    String comment
) {}
