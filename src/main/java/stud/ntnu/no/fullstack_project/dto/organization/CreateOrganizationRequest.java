package stud.ntnu.no.fullstack_project.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating an organization.
 *
 * @param name               display name of the organization
 * @param organizationNumber official organization / registration number
 * @param address            street address of the organization
 * @param phone              phone number of the organization
 * @param type               organization type enum value (e.g. RESTAURANT)
 */
@Schema(description = "Request payload for creating or updating an organization.")
public record CreateOrganizationRequest(
    @Schema(description = "Display name of the organization.", example = "Everest Sushi & Fusion")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Official organization or registration number.", example = "937219997")
    @Size(max = 50)
    String organizationNumber,

    @Schema(description = "Street address of the organization.", example = "Trondheim, Norway")
    @Size(max = 500)
    String address,

    @Schema(description = "Phone number of the organization.", example = "+47 123 45 678")
    @Size(max = 20)
    String phone,

    @Schema(description = "Type of organization.", example = "RESTAURANT")
    @NotNull
    String type
) {}
