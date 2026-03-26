package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload entry for a single day's serving hours.
 */
@Schema(description = "A serving hours entry for a specific weekday.")
public record ServingHoursEntry(
    @Schema(description = "Day of the week.", example = "MON")
    @NotNull
    String weekday,

    @Schema(description = "Serving start time (HH:mm).", example = "11:00")
    @NotNull
    String startTime,

    @Schema(description = "Serving end time (HH:mm).", example = "02:00")
    @NotNull
    String endTime,

    @Schema(description = "Minutes after end time for consumption deadline.", example = "30")
    Integer consumptionDeadlineMinutesAfterEnd
) {}
