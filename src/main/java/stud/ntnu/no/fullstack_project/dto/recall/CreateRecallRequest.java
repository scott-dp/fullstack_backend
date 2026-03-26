package stud.ntnu.no.fullstack_project.dto.recall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new recall case.
 *
 * @param title       recall case title
 * @param supplierId  optional supplier identifier
 * @param productName optional product name
 * @param batchLot    optional batch or lot number
 * @param description detailed description of the recall
 */
@Schema(description = "Request payload for creating a new recall case.")
public record CreateRecallRequest(
    @Schema(description = "Recall case title.", example = "Contaminated salmon batch")
    @NotBlank @Size(max = 255)
    String title,

    @Schema(description = "Supplier identifier.", example = "1")
    Long supplierId,

    @Schema(description = "Product name.", example = "Atlantic Salmon Fillet")
    String productName,

    @Schema(description = "Batch or lot number.", example = "LOT-2025-0042")
    String batchLot,

    @Schema(description = "Detailed description of the recall.", example = "Potential listeria contamination detected in batch.")
    @NotBlank @Size(max = 2000)
    String description
) {}
