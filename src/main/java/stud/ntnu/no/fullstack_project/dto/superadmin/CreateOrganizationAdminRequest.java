package stud.ntnu.no.fullstack_project.dto.superadmin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating an organization and inviting its first admin.")
public record CreateOrganizationAdminRequest(
    @Schema(description = "Display name of the organization.", example = "North Peak Bistro")
    @NotBlank @Size(max = 255)
    String organizationName,

    @Schema(description = "Optional organization number.", example = "123456789")
    @Size(max = 50)
    String organizationNumber,

    @Schema(description = "Type of organization.", example = "RESTAURANT")
    @NotBlank
    String organizationType,

    @Schema(description = "Admin first name.", example = "Ava")
    @NotBlank @Size(max = 100)
    String firstName,

    @Schema(description = "Admin last name.", example = "Nilsen")
    @Size(max = 100)
    String lastName,

    @Schema(description = "Admin email address.", example = "ava@example.com")
    @NotBlank @Email @Size(max = 255)
    String email
) {}
