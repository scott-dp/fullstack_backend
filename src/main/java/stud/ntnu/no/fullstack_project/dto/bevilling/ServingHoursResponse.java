package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

/**
 * Response payload representing serving hours for a specific weekday.
 */
@Schema(description = "Response representing serving hours for a weekday.")
public record ServingHoursResponse(
    @Schema(description = "Unique identifier.", example = "1")
    Long id,

    @Schema(description = "Day of the week.", example = "MON")
    String weekday,

    @Schema(description = "Serving start time.", example = "11:00")
    LocalTime startTime,

    @Schema(description = "Serving end time.", example = "02:00")
    LocalTime endTime,

    @Schema(description = "Minutes after end time for consumption deadline.", example = "30")
    int consumptionDeadlineMinutesAfterEnd
) {}
