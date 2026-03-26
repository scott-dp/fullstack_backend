package stud.ntnu.no.fullstack_project.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stud.ntnu.no.fullstack_project.dto.attachment.AttachmentResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Attachment;
import stud.ntnu.no.fullstack_project.repository.AttachmentRepository;

/**
 * Service for managing file attachments.
 *
 * <p>Handles uploading, retrieving, and deleting file attachments stored
 * on the local filesystem with metadata persisted in the database.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      "pdf", "jpg", "jpeg", "png", "gif", "doc", "docx", "xls", "xlsx"
  );

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  /**
   * Uploads a file attachment, storing it on disk and saving metadata to the database.
   *
   * @param file        the multipart file to upload
   * @param currentUser the authenticated user performing the upload
   * @return the created attachment response
   */
  @Transactional
  public AttachmentResponse uploadAttachment(MultipartFile file, AppUser currentUser) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File must not be empty");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = getExtension(originalFilename);
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new IllegalArgumentException(
          "File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
    }

    String storageKey = UUID.randomUUID() + "." + extension;

    try {
      Path uploadPath = Paths.get(uploadDir);
      Files.createDirectories(uploadPath);
      Path filePath = uploadPath.resolve(storageKey);
      Files.copy(file.getInputStream(), filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file", e);
    }

    Attachment attachment = new Attachment();
    attachment.setOrganization(currentUser.getOrganization());
    attachment.setStorageKey(storageKey);
    attachment.setOriginalFilename(originalFilename);
    attachment.setMimeType(file.getContentType());
    attachment.setSize(file.getSize());
    attachment.setUploadedBy(currentUser);

    Attachment saved = attachmentRepository.save(attachment);
    log.info("Attachment uploaded: {} (id={})", saved.getOriginalFilename(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves an attachment entity by its ID.
   *
   * @param id the attachment identifier
   * @return the attachment entity
   */
  public Attachment getAttachment(Long id) {
    return attachmentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attachment not found with id: " + id));
  }

  /**
   * Deletes an attachment from disk and database.
   *
   * @param id the attachment identifier
   */
  @Transactional
  public void deleteAttachment(Long id) {
    Attachment attachment = attachmentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attachment not found with id: " + id));

    try {
      Path filePath = Paths.get(uploadDir).resolve(attachment.getStorageKey());
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      log.warn("Failed to delete file from disk: {}", attachment.getStorageKey(), e);
    }

    attachmentRepository.delete(attachment);
    log.info("Attachment deleted: id={}", id);
  }

  /**
   * Maps an attachment entity to its response DTO.
   *
   * @param attachment the attachment entity
   * @return the attachment response DTO
   */
  private AttachmentResponse mapToResponse(Attachment attachment) {
    return new AttachmentResponse(
        attachment.getId(),
        attachment.getOriginalFilename(),
        attachment.getMimeType(),
        attachment.getSize(),
        attachment.getUploadedBy().getUsername(),
        attachment.getUploadedAt()
    );
  }

  /**
   * Extracts the file extension from the given filename.
   *
   * @param filename the original filename
   * @return the file extension without the dot
   */
  private String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      throw new IllegalArgumentException("File must have an extension");
    }
    return filename.substring(filename.lastIndexOf('.') + 1);
  }
}
