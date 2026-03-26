package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used for registering a new account.
 *
 * @param username unique username for the new account
 * @param email unique email address used for verification and optional login
 * @param password raw password supplied by the client
 */
@Schema(description = "Request payload containing registration details.")
public record RegisterRequest(
    @Schema(description = "Unique username used to identify the user account.", example = "scott")
    @NotBlank @Size(min = 3, max = 50)
    String username,

    @Schema(description = "Unique email address used for verification and login.", example = "scott@example.com")
    @NotBlank @Email @Size(max = 255)
    String email,

    @Schema(description = "Raw password provided by the client. It is hashed before persistence.", example = "superSecret123")
    @NotBlank @Size(min = 6, max = 255)
    String password
) {}
