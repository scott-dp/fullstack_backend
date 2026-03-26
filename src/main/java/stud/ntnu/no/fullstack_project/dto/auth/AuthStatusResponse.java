package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;

/**
 * Response payload indicating whether the caller is currently authenticated.
 *
 * <p>When {@code authenticated} is {@code true} the {@code user} field contains
 * the current user's profile; otherwise it is {@code null}.</p>
 *
 * @param authenticated whether the caller holds a valid session
 * @param user          profile of the authenticated user, or {@code null}
 */
@Schema(description = "Response indicating the current authentication status of the caller.")
public record AuthStatusResponse(
    @Schema(description = "Whether the caller holds a valid authentication cookie.", example = "true")
    boolean authenticated,

    @Schema(description = "Profile of the authenticated user, or null if not authenticated.")
    CurrentUserResponse user
) {}
