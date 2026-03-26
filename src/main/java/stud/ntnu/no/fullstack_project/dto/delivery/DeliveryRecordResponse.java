package stud.ntnu.no.fullstack_project.dto.delivery;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a delivery record with its line items.
 *
 * @param id                 unique identifier
 * @param supplierId         supplier identifier
 * @param supplierName       supplier name
 * @param deliveryDate       date of delivery
 * @param documentNumber     document / invoice number
 * @param receivedByUsername username of the person who received the delivery
 * @param notes              additional notes
 * @param attachmentId       attachment identifier
 * @param items              line items
 * @param createdAt          creation timestamp
 */
@Schema(description = "Response representing a delivery record with its line items.")
public record DeliveryRecordResponse(
    @Schema(description = "Unique identifier.", example = "1")
    Long id,

    @Schema(description = "Supplier identifier.", example = "1")
    Long supplierId,

    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    String supplierName,

    @Schema(description = "Date of delivery.", example = "2025-02-20")
    LocalDate deliveryDate,

    @Schema(description = "Document or invoice number.", example = "INV-2025-100")
    String documentNumber,

    @Schema(description = "Username of the person who received the delivery.", example = "staff")
    String receivedByUsername,

    @Schema(description = "Additional notes about the delivery.")
    String notes,

    @Schema(description = "Attachment identifier for supporting documents.")
    Long attachmentId,

    @ArraySchema(schema = @Schema(implementation = DeliveryItemResponse.class))
    List<DeliveryItemResponse> items,

    @Schema(description = "Creation timestamp.", example = "2025-02-20T10:00:00")
    LocalDateTime createdAt
) {}
