package stud.ntnu.no.fullstack_project.dto.health;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lightweight health payload returned by the public health endpoint.
 *
 * @param status current health status of the API
 * @param application application name resolved from Spring configuration
 */
@Schema(description = "Basic health response for the backend API.")
public record HealthResponse(
    @Schema(description = "Status of the application.", example = "UP")
    String status,

    @Schema(description = "Configured Spring application name.", example = "fullstack-project")
    String application
) {
}
