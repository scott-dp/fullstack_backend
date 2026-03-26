package stud.ntnu.no.fullstack_project.dto.recall;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a recall case.
 *
 * @param id           unique identifier
 * @param title        recall case title
 * @param supplierId   supplier identifier
 * @param supplierName supplier name
 * @param productName  product name
 * @param batchLot     batch or lot number
 * @param description  detailed description
 * @param openedAt     timestamp when the case was opened
 * @param closedAt     timestamp when the case was closed
 * @param status       current recall status
 */
@Schema(description = "Response representing a recall case.")
public record RecallCaseResponse(
    @Schema(description = "Unique identifier.", example = "1")
    Long id,

    @Schema(description = "Recall case title.", example = "Contaminated salmon batch")
    String title,

    @Schema(description = "Supplier identifier.", example = "1")
    Long supplierId,

    @Schema(description = "Supplier name.", example = "Norsk Sjømat AS")
    String supplierName,

    @Schema(description = "Product name.", example = "Atlantic Salmon Fillet")
    String productName,

    @Schema(description = "Batch or lot number.", example = "LOT-2025-0042")
    String batchLot,

    @Schema(description = "Detailed description of the recall.")
    String description,

    @Schema(description = "Timestamp when the case was opened.", example = "2025-02-20T10:00:00")
    LocalDateTime openedAt,

    @Schema(description = "Timestamp when the case was closed.", example = "2025-02-25T16:00:00")
    LocalDateTime closedAt,

    @Schema(description = "Current recall status.", example = "OPEN")
    String status
) {}
