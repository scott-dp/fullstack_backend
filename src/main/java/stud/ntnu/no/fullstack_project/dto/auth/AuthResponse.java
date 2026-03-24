package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;

/**
 * Response payload returned after a successful authentication or registration.
 *
 * <p>Wraps a human-readable message together with the authenticated user's profile.</p>
 *
 * @param message human-readable status message
 * @param user    profile details of the authenticated user
 */
@Schema(description = "Response returned after a successful authentication or registration.")
public record AuthResponse(
    @Schema(description = "Human-readable status message.", example = "Authentication successful")
    String message,

    @Schema(description = "Profile details of the authenticated user.")
    CurrentUserResponse user
) {}
