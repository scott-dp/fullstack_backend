package stud.ntnu.no.fullstack_project.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Standard error response body returned by the API when a request fails.
 *
 * <p>Contains a timestamp, HTTP status code, a human-readable message, and an
 * optional map of field-level validation errors.</p>
 *
 * @param timestamp when the error occurred
 * @param status    HTTP status code
 * @param message   human-readable error description
 * @param errors    field-level validation errors (may be empty)
 */
@Schema(description = "Standard error response body returned when a request fails.")
public record ApiError(
    @Schema(description = "Timestamp when the error occurred.", example = "2025-01-15T10:30:00Z")
    Instant timestamp,

    @Schema(description = "HTTP status code.", example = "400")
    int status,

    @Schema(description = "Human-readable error description.", example = "Validation failed")
    String message,

    @Schema(description = "Map of field-level validation errors. Keys are field names, values are error messages.")
    Map<String, String> errors
) {}
