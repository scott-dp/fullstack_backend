package stud.ntnu.no.fullstack_project.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating the authenticated user's profile fields.
 *
 * <p>All fields are optional; only non-null values will be applied.</p>
 *
 * @param firstName new first name, or {@code null} to keep unchanged
 * @param lastName  new last name, or {@code null} to keep unchanged
 * @param email     new email address, or {@code null} to keep unchanged
 */
@Schema(description = "Request payload for updating the authenticated user's profile.")
public record UpdateUserRequest(
    @Schema(description = "New first name for the user.", example = "Scott")
    @Size(max = 100)
    String firstName,

    @Schema(description = "New last name for the user.", example = "Liddell")
    @Size(max = 100)
    String lastName,

    @Schema(description = "New email address for the user.", example = "scott@example.com")
    @Email @Size(max = 255)
    String email
) {}
