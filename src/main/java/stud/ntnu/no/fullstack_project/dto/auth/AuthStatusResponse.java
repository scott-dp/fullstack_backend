package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;

/**
 * Response payload for authentication status checks.
 *
 * @param authenticated whether the current request is associated with a valid auth cookie
 * @param user the resolved authenticated user, or {@code null} if anonymous
 */
@Schema(description = "Authentication status response for the current request context.")
public record AuthStatusResponse(
    @Schema(description = "Whether the current request is authenticated.", example = "true")
    boolean authenticated,

    @Schema(description = "Authenticated user details when a valid auth cookie is present.")
    CurrentUserResponse user
) {
}
