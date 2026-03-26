package stud.ntnu.no.fullstack_project.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Response payload for a single delivery line item.
 *
 * @param id                    unique identifier
 * @param productName           product name
 * @param quantity              quantity received
 * @param unit                  unit of measure
 * @param batchLot              batch or lot number
 * @param expiryDate            expiry date
 * @param internalIngredientRef internal ingredient reference
 */
@Schema(description = "Response representing a single delivery line item.")
public record DeliveryItemResponse(
    @Schema(description = "Unique identifier.", example = "1")
    Long id,

    @Schema(description = "Product name.", example = "Atlantic Salmon Fillet")
    String productName,

    @Schema(description = "Quantity received.", example = "50")
    String quantity,

    @Schema(description = "Unit of measure.", example = "kg")
    String unit,

    @Schema(description = "Batch or lot number.", example = "LOT-2025-0042")
    String batchLot,

    @Schema(description = "Expiry date.", example = "2025-03-15")
    LocalDate expiryDate,

    @Schema(description = "Internal ingredient reference.", example = "ING-001")
    String internalIngredientRef
) {}
