package stud.ntnu.no.fullstack_project.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Standard error payload returned by the global exception handler.
 *
 * @param timestamp timestamp of when the error response was generated
 * @param status HTTP status code of the response
 * @param message top-level error message
 * @param errors optional field-level validation errors
 */
@Schema(description = "Standardized error payload returned when a request fails.")
public record ApiError(
    @Schema(description = "Timestamp of the error response.", example = "2026-03-23T14:20:00Z")
    Instant timestamp,

    @Schema(description = "HTTP status code of the response.", example = "400")
    int status,

    @Schema(description = "Top-level error message.", example = "Validation failed")
    String message,

    @Schema(description = "Optional map of field-specific validation errors.")
    Map<String, String> errors
) {
}
