package stud.ntnu.no.fullstack_project.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing supplier. All fields are optional.
 *
 * @param name               supplier name
 * @param organizationNumber organization number
 * @param contactName        contact person name
 * @param email              email address
 * @param phone              phone number
 * @param address            address
 * @param notes              additional notes
 * @param active             whether the supplier is active
 */
@Schema(description = "Request payload for updating an existing supplier. All fields are optional.")
public record UpdateSupplierRequest(
    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    @Size(max = 255)
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
    @Size(max = 2000)
    String notes,

    @Schema(description = "Whether the supplier is active.", example = "true")
    Boolean active
) {}
