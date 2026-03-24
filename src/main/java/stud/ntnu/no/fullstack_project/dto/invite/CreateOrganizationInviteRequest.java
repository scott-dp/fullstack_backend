package stud.ntnu.no.fullstack_project.dto.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for generating a new organization invite token.
 *
 * @param role role that will be granted when the invitation is accepted
 * @param organizationId target organization; required for admins and ignored for managers
 * @param expiresInDays validity period in days for the invite token
 */
@Schema(description = "Request payload for creating a new organization invitation.")
public record CreateOrganizationInviteRequest(
    @NotBlank
    @Schema(description = "Role to assign when the invite is accepted.", example = "ROLE_MANAGER")
    String role,

    @Schema(description = "Target organization ID. Required for admins, ignored for managers.", example = "1")
    Long organizationId,

    @NotNull
    @Schema(description = "Number of days before the invitation expires.", example = "7")
    Integer expiresInDays
) {}
