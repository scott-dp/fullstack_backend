package stud.ntnu.no.fullstack_project.dto.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload describing an organization invitation token.
 *
 * @param id invite identifier
 * @param token token value to share with the invitee
 * @param organizationId target organization identifier
 * @param organizationName target organization name
 * @param role role that will be granted upon acceptance
 * @param createdByUsername username of the inviter
 * @param createdAt when the invite was created
 * @param expiresAt when the invite expires
 * @param accepted whether the invite has been accepted
 * @param acceptedByUsername username of the accepting user, if any
 * @param revoked whether the invite has been revoked
 */
@Schema(description = "Organization invitation metadata.")
public record OrganizationInviteResponse(
    @Schema(description = "Invite identifier.", example = "1")
    Long id,

    @Schema(description = "Single-use token to share with the invitee.", example = "4d27358d-fd4c-4258-bc66-2fd197b56e66")
    String token,

    @Schema(description = "Target organization identifier.", example = "1")
    Long organizationId,

    @Schema(description = "Target organization name.", example = "Everest Sushi & Fusion")
    String organizationName,

    @Schema(description = "Role that will be granted upon acceptance.", example = "ROLE_STAFF")
    String role,

    @Schema(description = "Username of the user who created the invite.", example = "manager")
    String createdByUsername,

    @Schema(description = "Timestamp when the invite was created.")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp when the invite expires.")
    LocalDateTime expiresAt,

    @Schema(description = "Whether the invite has already been accepted.", example = "false")
    boolean accepted,

    @Schema(description = "Username of the user who accepted the invite, if any.", example = "scott")
    String acceptedByUsername,

    @Schema(description = "Whether the invite has been revoked.", example = "false")
    boolean revoked
) {}
