package stud.ntnu.no.fullstack_project.dto.user;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Response payload describing the currently authenticated user.
 *
 * @param id unique database identifier of the user
 * @param username unique username of the authenticated user
 * @param roles granted Spring Security roles for the user
 */
@Schema(description = "Representation of the currently authenticated user.")
public record CurrentUserResponse(
    @Schema(description = "Database identifier of the user.", example = "1")
    Long id,

    @Schema(description = "Username of the authenticated user.", example = "scott")
    String username,

    @ArraySchema(schema = @Schema(description = "Granted Spring Security role.", example = "ROLE_USER"))
    Set<String> roles
) {
}
