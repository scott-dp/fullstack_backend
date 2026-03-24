package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned after verifying an email token.
 *
 * @param message verification result message
 */
@Schema(description = "Response returned after an email verification attempt.")
public record VerificationResponse(
    @Schema(description = "Human-readable verification status message.",
        example = "Email verified successfully. You can now log in.")
    String message
) {}
