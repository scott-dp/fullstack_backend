package stud.ntnu.no.fullstack_project.dto.user;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request payload used by administrators to create a new user account.
 *
 * <p>Unlike self-registration, this allows specifying the target organization
 * and one or more security roles.</p>
 *
 * @param username       desired login name for the new user
 * @param password       raw password (will be encoded before persistence)
 * @param firstName      first name, may be {@code null}
 * @param lastName       last name, may be {@code null}
 * @param email          email address, may be {@code null}
 * @param roles          set of role names to assign (e.g. ROLE_STAFF)
 * @param organizationId ID of the organization the user will belong to
 */
@Schema(description = "Request payload used by an administrator to create a new user account.")
public record AdminCreateUserRequest(
    @Schema(description = "Desired login name for the new user.", example = "newuser")
    @NotBlank @Size(min = 3, max = 50)
    String username,

    @Schema(description = "Raw password that will be encoded before storage.", example = "securePass123")
    @NotBlank @Size(min = 6, max = 255)
    String password,

    @Schema(description = "First name of the new user.", example = "John")
    @Size(max = 100)
    String firstName,

    @Schema(description = "Last name of the new user.", example = "Doe")
    @Size(max = 100)
    String lastName,

    @Schema(description = "Email address of the new user.", example = "john.doe@example.com")
    @Email @Size(max = 255)
    String email,

    @ArraySchema(schema = @Schema(description = "Spring Security role to assign.", example = "ROLE_STAFF"))
    @NotNull
    Set<String> roles,

    @Schema(description = "ID of the organization the user will belong to.", example = "1")
    @NotNull
    Long organizationId
) {}
