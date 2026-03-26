package stud.ntnu.no.fullstack_project.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a supplier.
 *
 * @param id                 unique supplier identifier
 * @param name               supplier name
 * @param organizationNumber organization number
 * @param contactName        contact person name
 * @param email              email address
 * @param phone              phone number
 * @param address            address
 * @param notes              additional notes
 * @param active             whether the supplier is active
 * @param createdAt          creation timestamp
 * @param updatedAt          last update timestamp
 */
@Schema(description = "Response representing a supplier.")
public record SupplierResponse(
    @Schema(description = "Unique supplier identifier.", example = "1")
    Long id,

    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    String name,

    @Schema(description = "Organization number.", example = "912345678")
    String organizationNumber,

    @Schema(description = "Contact person name.", example = "Ola Nordmann")
    String contactName,

    @Schema(description = "Email address.", example = "kontakt@norsksjømat.no")
    String email,

    @Schema(description = "Phone number.", example = "+47 22 33 44 55")
    String phone,

    @Schema(description = "Address.", example = "Bryggen 1, Bergen")
    String address,

    @Schema(description = "Additional notes about the supplier.")
    String notes,

    @Schema(description = "Whether the supplier is active.", example = "true")
    boolean active,

    @Schema(description = "Creation timestamp.", example = "2025-01-15T08:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp.", example = "2025-01-15T10:00:00")
    LocalDateTime updatedAt
) {}
