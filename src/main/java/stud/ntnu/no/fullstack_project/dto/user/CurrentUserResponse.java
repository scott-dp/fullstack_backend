package stud.ntnu.no.fullstack_project.dto.user;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Response payload containing the full profile of the currently authenticated user.
 *
 * <p>Includes identity fields, contact information, granted roles, and the
 * organization the user belongs to.</p>
 *
 * @param id               unique user identifier
 * @param username         login name of the user
 * @param firstName        first name, may be {@code null}
 * @param lastName         last name, may be {@code null}
 * @param email            email address, may be {@code null}
 * @param roles            set of granted Spring Security roles
 * @param organizationId   ID of the user's organization, or {@code null}
 * @param organizationName display name of the user's organization, or {@code null}
 */
@Schema(description = "Full profile of the currently authenticated user.")
public record CurrentUserResponse(
    @Schema(description = "Unique user identifier.", example = "1")
    Long id,

    @Schema(description = "Login name of the user.", example = "scott")
    String username,

    @Schema(description = "First name of the user.", example = "Scott")
    String firstName,

    @Schema(description = "Last name of the user.", example = "Liddell")
    String lastName,

    @Schema(description = "Email address of the user.", example = "scott@example.com")
    String email,

    @ArraySchema(schema = @Schema(description = "Granted Spring Security role.", example = "ROLE_STAFF"))
    Set<String> roles,

    @Schema(description = "ID of the organization the user belongs to.", example = "1")
    Long organizationId,

    @Schema(description = "Display name of the user's organization.", example = "Everest Sushi & Fusion")
    String organizationName
) {}
