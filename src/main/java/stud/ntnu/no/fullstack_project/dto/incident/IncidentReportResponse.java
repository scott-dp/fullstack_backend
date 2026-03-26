package stud.ntnu.no.fullstack_project.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Summary report of alcohol incidents for an organization.
 *
 * @param totalIncidents total number of incidents
 * @param openCount      number of open incidents
 * @param closedCount    number of closed incidents
 * @param byType         breakdown of incident counts by incident type
 */
@Schema(description = "Summary report of alcohol incidents for an organization.")
public record IncidentReportResponse(
    @Schema(description = "Total number of incidents.", example = "25")
    long totalIncidents,

    @Schema(description = "Number of open incidents.", example = "10")
    long openCount,

    @Schema(description = "Number of closed incidents.", example = "12")
    long closedCount,

    @Schema(description = "Incident counts grouped by type.")
    Map<String, Long> byType
) {}
