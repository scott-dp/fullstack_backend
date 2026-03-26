package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used for registration and login operations.
 *
 * <p>Contains the minimum credential set required by the authentication API.</p>
 *
 * @param username the unique username supplied by the client
 * @param password the raw password supplied by the client
 */
@Schema(description = "Request payload containing credentials for registration or login.")
public record AuthRequest(
    @Schema(description = "Unique username used to identify the user account.", example = "scott")
    @NotBlank @Size(min = 3, max = 50)
    String username,

    @Schema(description = "Raw password provided by the client. It is hashed before persistence.", example = "superSecret123")
    @NotBlank @Size(min = 6, max = 255)
    String password
) {}
