package stud.ntnu.no.fullstack_project.dto.deviation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for reporting a new deviation.
 *
 * @param title       short summary of the deviation
 * @param description detailed description of the deviation
 * @param category    compliance category (e.g. FOOD_SAFETY)
 * @param severity    severity level (e.g. LOW, MEDIUM, HIGH, CRITICAL)
 */
@Schema(description = "Request payload for reporting a new deviation.")
public record CreateDeviationRequest(
    @Schema(description = "Short summary of the deviation.", example = "Fridge temperature too high")
    @NotBlank @Size(max = 255)
    String title,

    @Schema(description = "Detailed description of the deviation.", example = "Walk-in fridge measured at 8C during morning check.")
    @NotBlank @Size(max = 2000)
    String description,

    @Schema(description = "Compliance category the deviation belongs to.", example = "FOOD_SAFETY")
    @NotNull
    String category,

    @Schema(description = "Severity level of the deviation.", example = "HIGH")
    @NotNull
    String severity
) {}
