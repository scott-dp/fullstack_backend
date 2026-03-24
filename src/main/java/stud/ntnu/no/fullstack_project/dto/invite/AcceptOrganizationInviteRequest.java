package stud.ntnu.no.fullstack_project.dto.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used by an authenticated user to accept an invitation token.
 *
 * @param token invitation token string
 */
@Schema(description = "Request payload for accepting an organization invite.")
public record AcceptOrganizationInviteRequest(
    @NotBlank
    @Schema(description = "Single-use invitation token.", example = "4d27358d-fd4c-4258-bc66-2fd197b56e66")
    String token
) {}
