package stud.ntnu.no.fullstack_project.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing an organization record.
 *
 * @param id                 unique organization identifier
 * @param name               display name
 * @param organizationNumber official registration number
 * @param address            street address
 * @param phone              phone number
 * @param type               organization type enum name
 * @param createdAt          timestamp when the organization was created
 */
@Schema(description = "Response payload representing an organization.")
public record OrganizationResponse(
    @Schema(description = "Unique organization identifier.", example = "1")
    Long id,

    @Schema(description = "Display name of the organization.", example = "Everest Sushi & Fusion")
    String name,

    @Schema(description = "Official organization or registration number.", example = "937219997")
    String organizationNumber,

    @Schema(description = "Street address of the organization.", example = "Trondheim, Norway")
    String address,

    @Schema(description = "Phone number of the organization.", example = "+47 123 45 678")
    String phone,

    @Schema(description = "Type of organization.", example = "RESTAURANT")
    String type,

    @Schema(description = "Timestamp when the organization was created.", example = "2025-01-15T10:30:00")
    LocalDateTime createdAt
) {}
