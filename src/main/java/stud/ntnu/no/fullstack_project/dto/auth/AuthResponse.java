package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;

/**
 * Response payload returned after successful authentication-related operations.
 *
 * @param message human-readable status message for the client
 * @param user the authenticated or newly registered user
 */
@Schema(description = "Authentication response returned after a successful login or registration.")
public record AuthResponse(
    @Schema(description = "Human-readable status message.", example = "Authentication successful")
    String message,

    @Schema(description = "Representation of the authenticated user returned to the client.")
    CurrentUserResponse user
) {
}
