package stud.ntnu.no.fullstack_project.entity.operations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;

/**
 * Represents an uploaded file attachment with metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attachments")
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(name = "storage_key", nullable = false)
  private String storageKey;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "mime_type", nullable = false)
  private String mimeType;

  @Column(nullable = false)
  private Long size;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by", nullable = false)
  private AppUser uploadedBy;

  @CreationTimestamp
  @Column(name = "uploaded_at", updatable = false)
  private LocalDateTime uploadedAt;
}
