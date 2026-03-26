package stud.ntnu.no.fullstack_project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information about a pending admin account setup invite.")
public record AdminSetupInfoResponse(
    @Schema(example = "ava@example.com")
    String email,

    @Schema(example = "Ava")
    String firstName,

    @Schema(example = "Nilsen")
    String lastName,

    @Schema(example = "North Peak Bistro")
    String organizationName
) {}
