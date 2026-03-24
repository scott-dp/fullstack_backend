package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Simple message response payload.
 *
 * @param message human-readable status message
 */
@Schema(description = "Simple message response payload.")
public record MessageResponse(
    @Schema(description = "Human-readable status message.", example = "A login code has been sent to your email.")
    String message
) {}
