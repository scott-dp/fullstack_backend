package stud.ntnu.no.fullstack_project.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for closing an alcohol incident.
 *
 * @param notes optional closing notes
 */
@Schema(description = "Request payload for closing an alcohol incident.")
public record CloseIncidentRequest(
    @Schema(description = "Optional closing notes.", example = "Issue resolved, no further action needed.")
    String notes
) {}
