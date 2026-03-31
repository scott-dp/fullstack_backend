package stud.ntnu.no.fullstack_project.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import stud.ntnu.no.fullstack_project.dto.attachment.AttachmentResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.Attachment;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.AttachmentService;

/**
 * REST controller for file attachment management.
 *
 * <p>Provides endpoints to upload, download, and delete file attachments.</p>
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attachments", description = "Endpoints for file attachment management")
public class AttachmentController {

  private final AttachmentService attachmentService;

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Upload a file attachment",
      description = "Uploads a file and stores metadata. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "File uploaded successfully",
          content = @Content(schema = @Schema(implementation = AttachmentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid file or file type not allowed",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AttachmentResponse> uploadAttachment(
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Uploading attachment: {} by user={}", file.getOriginalFilename(),
        currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(attachmentService.uploadAttachment(file, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Download a file attachment",
      description = "Streams the file content for download."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File streamed successfully"),
      @ApiResponse(responseCode = "400", description = "Attachment not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
    log.info("Downloading attachment id={}", id);
    Attachment attachment = attachmentService.getAttachment(id);

    try {
      Path filePath = Paths.get(uploadDir).resolve(attachment.getStorageKey());
      Resource resource = new UrlResource(filePath.toUri());

      if (!resource.exists()) {
        throw new IllegalArgumentException("File not found on disk for attachment id: " + id);
      }

      String contentType = attachment.getMimeType();
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
          .body(resource);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file", e);
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Delete a file attachment",
      description = "Deletes a file from disk and database. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Attachment not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
    log.info("Deleting attachment id={}", id);
    attachmentService.deleteAttachment(id);
    return ResponseEntity.noContent().build();
  }
}
