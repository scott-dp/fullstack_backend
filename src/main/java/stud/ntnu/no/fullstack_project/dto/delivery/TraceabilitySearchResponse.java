package stud.ntnu.no.fullstack_project.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Response payload for a traceability search result.
 *
 * @param deliveryItemId   delivery item identifier
 * @param deliveryRecordId delivery record identifier
 * @param supplierName     supplier name
 * @param productName      product name
 * @param batchLot         batch or lot number
 * @param deliveryDate     date of delivery
 * @param expiryDate       expiry date
 */
@Schema(description = "Response representing a traceability search result.")
public record TraceabilitySearchResponse(
    @Schema(description = "Delivery item identifier.", example = "1")
    Long deliveryItemId,

    @Schema(description = "Delivery record identifier.", example = "1")
    Long deliveryRecordId,

    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    String supplierName,

    @Schema(description = "Product name.", example = "Atlantic Salmon Fillet")
    String productName,

    @Schema(description = "Batch or lot number.", example = "LOT-2025-0042")
    String batchLot,

    @Schema(description = "Date of delivery.", example = "2025-02-20")
    LocalDate deliveryDate,

    @Schema(description = "Expiry date.", example = "2025-03-15")
    LocalDate expiryDate
) {}
