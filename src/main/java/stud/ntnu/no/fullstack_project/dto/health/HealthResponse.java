package stud.ntnu.no.fullstack_project.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Simple health-check response indicating the application is running.
 *
 * @param status      current health status indicator
 * @param application name of the running application
 */
@Schema(description = "Health-check response indicating application status.")
public record HealthResponse(
    @Schema(description = "Current health status of the application.", example = "UP")
    String status,

    @Schema(description = "Name of the running application.", example = "fullstack-project")
    String application
) {}
