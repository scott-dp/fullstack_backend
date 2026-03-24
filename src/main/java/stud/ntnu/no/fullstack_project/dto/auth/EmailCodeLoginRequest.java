package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for authenticating with an emailed one-time code.
 *
 * @param email verified account email
 * @param code one-time numeric login code
 */
@Schema(description = "Request payload for authenticating with email and one-time code.")
public record EmailCodeLoginRequest(
    @Schema(description = "Verified email address for the target account.", example = "scott@example.com")
    @NotBlank @Email @Size(max = 255)
    String email,

    @Schema(description = "One-time login code sent to the email address.", example = "482731")
    @NotBlank @Pattern(regexp = "\\d{6}")
    String code
) {}
