package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for sending a one-time login code to a verified email address.
 *
 * @param email verified email address that should receive the code
 */
@Schema(description = "Request payload for emailing a one-time login code.")
public record EmailCodeRequest(
    @Schema(description = "Verified email address for the target account.", example = "scott@example.com")
    @NotBlank @Email @Size(max = 255)
    String email
) {}
