package stud.ntnu.no.fullstack_project.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 50) String organizationNumber,
    @Size(max = 500) String address,
    @Size(max = 20) String phone,
    @NotNull String type
) {}
