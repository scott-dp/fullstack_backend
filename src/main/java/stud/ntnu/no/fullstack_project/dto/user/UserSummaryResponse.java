package stud.ntnu.no.fullstack_project.dto.user;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Lightweight user summary used in lists and look-ups where the full profile is not needed.
 *
 * @param id        unique user identifier
 * @param username  login name of the user
 * @param firstName first name, may be {@code null}
 * @param lastName  last name, may be {@code null}
 * @param roles     set of granted Spring Security roles
 */
@Schema(description = "Lightweight summary of a user account.")
public record UserSummaryResponse(
    @Schema(description = "Unique user identifier.", example = "1")
    Long id,

    @Schema(description = "Login name of the user.", example = "scott")
    String username,

    @Schema(description = "First name of the user.", example = "Scott")
    String firstName,

    @Schema(description = "Last name of the user.", example = "Liddell")
    String lastName,

    @ArraySchema(schema = @Schema(description = "Granted Spring Security role.", example = "ROLE_STAFF"))
    Set<String> roles
) {}
