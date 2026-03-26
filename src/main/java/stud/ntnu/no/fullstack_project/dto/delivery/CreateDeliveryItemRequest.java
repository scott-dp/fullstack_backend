package stud.ntnu.no.fullstack_project.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a single delivery line item.
 *
 * @param productName          product name
 * @param quantity             quantity received
 * @param unit                 unit of measure
 * @param batchLot             batch or lot number
 * @param expiryDate           expiry date (ISO format)
 * @param internalIngredientRef internal ingredient reference
 */
@Schema(description = "Request payload for a single delivery line item.")
public record CreateDeliveryItemRequest(
    @Schema(description = "Product name.", example = "Atlantic Salmon Fillet")
    @NotBlank @Size(max = 255)
    String productName,

    @Schema(description = "Quantity received.", example = "50")
    String quantity,

    @Schema(description = "Unit of measure.", example = "kg")
    String unit,

    @Schema(description = "Batch or lot number.", example = "LOT-2025-0042")
    String batchLot,

    @Schema(description = "Expiry date in ISO format.", example = "2025-03-15")
    String expiryDate,

    @Schema(description = "Internal ingredient reference.", example = "ING-001")
    String internalIngredientRef
) {}
