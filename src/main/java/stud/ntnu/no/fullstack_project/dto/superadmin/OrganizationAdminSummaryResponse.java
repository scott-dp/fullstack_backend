package stud.ntnu.no.fullstack_project.dto.superadmin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Summary of an organization admin account visible to superadmins.")
public record OrganizationAdminSummaryResponse(
    @Schema(example = "5")
    Long id,

    @Schema(example = "ava")
    String username,

    @Schema(example = "Ava")
    String firstName,

    @Schema(example = "Nilsen")
    String lastName,

    @Schema(example = "ava@example.com")
    String email,

    @Schema(example = "2")
    Long organizationId,

    @Schema(example = "North Peak Bistro")
    String organizationName,

    @Schema(example = "true")
    boolean active,

    @Schema(example = "false")
    boolean setupPending,

    @Schema(example = "2026-03-26T12:00:00")
    LocalDateTime createdAt
) {}
