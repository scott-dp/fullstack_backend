package stud.ntnu.no.fullstack_project.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for reporting a new alcohol incident.
 *
 * @param occurredAt           ISO-8601 date-time when the incident occurred
 * @param shiftLabel           optional shift label (e.g. "Evening Shift")
 * @param locationArea         optional area within the venue
 * @param incidentType         type of incident (e.g. AGE_DOUBT_REFUSAL)
 * @param severity             severity level (LOW, MEDIUM, HIGH, CRITICAL)
 * @param description          detailed description of the incident
 * @param immediateActionTaken optional description of immediate actions taken
 * @param followUpRequired     whether follow-up is needed
 * @param assignedToId         optional user ID to assign the incident to
 * @param linkedRoutineId      optional linked routine ID
 * @param linkedDeviationId    optional linked deviation ID
 */
@Schema(description = "Request payload for reporting a new alcohol incident.")
public record CreateAlcoholIncidentRequest(
    @Schema(description = "ISO-8601 date-time when the incident occurred.", example = "2026-03-25T22:30:00")
    @NotNull
    String occurredAt,

    @Schema(description = "Shift label.", example = "Evening Shift")
    String shiftLabel,

    @Schema(description = "Location area within the venue.", example = "Bar Area")
    String locationArea,

    @Schema(description = "Type of alcohol incident.", example = "AGE_DOUBT_REFUSAL")
    @NotNull
    String incidentType,

    @Schema(description = "Severity level.", example = "MEDIUM")
    @NotNull
    String severity,

    @Schema(description = "Detailed description of the incident.", example = "Customer could not provide valid ID when asked.")
    @NotBlank @Size(max = 2000)
    String description,

    @Schema(description = "Immediate action taken.", example = "Refused service and asked customer to leave.")
    @Size(max = 2000)
    String immediateActionTaken,

    @Schema(description = "Whether follow-up action is required.", example = "true")
    @NotNull
    Boolean followUpRequired,

    @Schema(description = "User ID to assign the incident to.", example = "2")
    Long assignedToId,

    @Schema(description = "Linked routine ID.", example = "5")
    Long linkedRoutineId,

    @Schema(description = "Linked deviation ID.", example = "3")
    Long linkedDeviationId
) {}
