package stud.ntnu.no.fullstack_project.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing an alcohol incident record.
 *
 * @param id                       unique incident identifier
 * @param occurredAt               when the incident occurred
 * @param reportedByUsername       username of the reporter
 * @param assignedToId             identifier of the assignee, or {@code null}
 * @param assignedToUsername       username of the assignee, or {@code null}
 * @param shiftLabel               shift label
 * @param locationArea             area within the venue
 * @param incidentType             type of incident
 * @param severity                 severity level
 * @param description              detailed description
 * @param immediateActionTaken     immediate action taken, or {@code null}
 * @param followUpRequired         whether follow-up is required
 * @param linkedRoutineId          linked routine ID, or {@code null}
 * @param linkedDeviationId        linked deviation ID, or {@code null}
 * @param status                   current status
 * @param closedAt                 timestamp when closed, or {@code null}
 * @param closedByUsername         username of the user who closed it, or {@code null}
 * @param createdAt                creation timestamp
 * @param updatedAt                last update timestamp
 */
@Schema(description = "Response representing an alcohol incident record.")
public record AlcoholIncidentResponse(
    @Schema(description = "Unique incident identifier.", example = "1")
    Long id,

    @Schema(description = "When the incident occurred.", example = "2026-03-25T22:30:00")
    LocalDateTime occurredAt,

    @Schema(description = "Username of the user who reported the incident.", example = "staff")
    String reportedByUsername,

    @Schema(description = "ID of the user the incident is assigned to.", example = "2")
    Long assignedToId,

    @Schema(description = "Username of the user the incident is assigned to.", example = "manager")
    String assignedToUsername,

    @Schema(description = "Shift label.", example = "Evening Shift")
    String shiftLabel,

    @Schema(description = "Location area within the venue.", example = "Bar Area")
    String locationArea,

    @Schema(description = "Type of alcohol incident.", example = "AGE_DOUBT_REFUSAL")
    String incidentType,

    @Schema(description = "Severity level.", example = "MEDIUM")
    String severity,

    @Schema(description = "Detailed description of the incident.")
    String description,

    @Schema(description = "Immediate action taken.")
    String immediateActionTaken,

    @Schema(description = "Whether follow-up action is required.", example = "true")
    boolean followUpRequired,

    @Schema(description = "Linked routine ID.", example = "5")
    Long linkedRoutineId,

    @Schema(description = "Linked deviation ID.", example = "3")
    Long linkedDeviationId,

    @Schema(description = "Current incident status.", example = "OPEN")
    String status,

    @Schema(description = "Timestamp when the incident was closed.", example = "2026-03-26T10:00:00")
    LocalDateTime closedAt,

    @Schema(description = "Username of the user who closed the incident.", example = "manager")
    String closedByUsername,

    @Schema(description = "Timestamp when the record was created.", example = "2026-03-25T22:35:00")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp when the record was last updated.", example = "2026-03-25T23:00:00")
    LocalDateTime updatedAt
) {}
