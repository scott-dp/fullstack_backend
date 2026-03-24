package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned after a successful registration request.
 *
 * @param message human-readable status message
 */
@Schema(description = "Response returned after a successful registration.")
public record RegistrationResponse(
    @Schema(description = "Human-readable registration status message.",
        example = "Registration successful. Verify your email before logging in.")
    String message
) {}
