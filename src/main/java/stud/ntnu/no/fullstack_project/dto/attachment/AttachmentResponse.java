package stud.ntnu.no.fullstack_project.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing an uploaded attachment.
 *
 * @param id                 unique identifier
 * @param originalFilename   original file name
 * @param mimeType           MIME type
 * @param size               file size in bytes
 * @param uploadedByUsername username of the uploader
 * @param uploadedAt         upload timestamp
 */
@Schema(description = "Response representing an uploaded attachment.")
public record AttachmentResponse(
    @Schema(description = "Unique identifier.", example = "1")
    Long id,

    @Schema(description = "Original file name.", example = "invoice.pdf")
    String originalFilename,

    @Schema(description = "MIME type.", example = "application/pdf")
    String mimeType,

    @Schema(description = "File size in bytes.", example = "102400")
    Long size,

    @Schema(description = "Username of the uploader.", example = "admin")
    String uploadedByUsername,

    @Schema(description = "Upload timestamp.", example = "2025-02-20T10:00:00")
    LocalDateTime uploadedAt
) {}
