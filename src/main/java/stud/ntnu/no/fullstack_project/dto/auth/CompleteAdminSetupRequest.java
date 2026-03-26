package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for completing invited admin account setup.")
public record CompleteAdminSetupRequest(
    @Schema(description = "One-time admin setup token.")
    @NotBlank
    String token,

    @Schema(description = "New password for the invited admin account.", example = "superSecret123")
    @NotBlank @Size(min = 8, max = 255)
    String password
) {}
