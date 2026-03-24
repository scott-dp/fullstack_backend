package stud.ntnu.no.fullstack_project.dto.temperature;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for recording a new temperature measurement.
 *
 * @param location     name of the location where the temperature was taken
 * @param temperature  measured temperature value in degrees Celsius
 * @param minThreshold minimum acceptable temperature threshold
 * @param maxThreshold maximum acceptable temperature threshold
 * @param comment      optional comment about the measurement
 */
@Schema(description = "Request payload for recording a new temperature measurement.")
public record CreateTemperatureLogRequest(
    @Schema(description = "Name of the location where the temperature was taken.", example = "Walk-in Fridge")
    @NotBlank @Size(max = 255)
    String location,

    @Schema(description = "Measured temperature value in degrees Celsius.", example = "3.5")
    double temperature,

    @Schema(description = "Minimum acceptable temperature threshold in degrees Celsius.", example = "0.0")
    double minThreshold,

    @Schema(description = "Maximum acceptable temperature threshold in degrees Celsius.", example = "4.0")
    double maxThreshold,

    @Schema(description = "Optional comment about the measurement.", example = "Measured after restocking.")
    @Size(max = 500)
    String comment
) {}
