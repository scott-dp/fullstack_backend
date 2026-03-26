package stud.ntnu.no.fullstack_project.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for recording a new delivery.
 *
 * @param supplierId     supplier identifier
 * @param deliveryDate   date of delivery (ISO format)
 * @param documentNumber optional document / invoice number
 * @param notes          optional notes
 * @param attachmentId   optional attachment identifier
 * @param items          line items in the delivery
 */
@Schema(description = "Request payload for recording a new delivery.")
public record CreateDeliveryRequest(
    @Schema(description = "Supplier identifier.", example = "1")
    @NotNull
    Long supplierId,

    @Schema(description = "Date of delivery in ISO format.", example = "2025-02-20")
    @NotNull
    String deliveryDate,

    @Schema(description = "Document or invoice number.", example = "INV-2025-100")
    String documentNumber,

    @Schema(description = "Additional notes about the delivery.")
    @Size(max = 2000)
    String notes,

    @Schema(description = "Attachment identifier for supporting documents.")
    Long attachmentId,

    @Schema(description = "Line items in the delivery.")
    @NotNull
    List<@Valid CreateDeliveryItemRequest> items
) {}
