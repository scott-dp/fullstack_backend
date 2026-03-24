package stud.ntnu.no.fullstack_project.dto.organization;

import java.time.LocalDateTime;

public record OrganizationResponse(
    Long id,
    String name,
    String organizationNumber,
    String address,
    String phone,
    String type,
    LocalDateTime createdAt
) {}
