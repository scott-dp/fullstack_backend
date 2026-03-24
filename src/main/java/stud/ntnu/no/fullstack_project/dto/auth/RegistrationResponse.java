package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned after a successful registration request.
 *
 * @param message human-readable status message
 * @param verificationLink verification URL to open before login is allowed
 */
@Schema(description = "Response returned after a successful registration.")
public record RegistrationResponse(
    @Schema(description = "Human-readable registration status message.",
        example = "Registration successful. Verify your email before logging in.")
    String message,

    @Schema(description = "Verification link to activate the account.",
        example = "http://localhost:5173/verify-email?token=123456")
    String verificationLink
) {}
