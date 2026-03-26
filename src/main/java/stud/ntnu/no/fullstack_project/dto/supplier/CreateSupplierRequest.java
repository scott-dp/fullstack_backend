package stud.ntnu.no.fullstack_project.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new supplier.
 *
 * @param name               supplier name
 * @param organizationNumber optional organization number
 * @param contactName        optional contact person name
 * @param email              optional email address
 * @param phone              optional phone number
 * @param address            optional address
 * @param notes              optional notes
 */
@Schema(description = "Request payload for creating a new supplier.")
public record CreateSupplierRequest(
    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    @NotBlank @Size(max = 255)
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
    String notes
) {}
