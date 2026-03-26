package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used for login operations.
 *
 * @param identifier username or email address used to resolve the account
 * @param password raw password supplied by the client
 */
@Schema(description = "Request payload containing login credentials.")
public record LoginRequest(
    @Schema(description = "Username or email used to identify the account.", example = "scott@example.com")
    @NotBlank @Size(min = 3, max = 255)
    String identifier,

    @Schema(description = "Raw password provided by the client.", example = "superSecret123")
    @NotBlank @Size(min = 6, max = 255)
    String password
) {}
