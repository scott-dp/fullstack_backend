package stud.ntnu.no.fullstack_project.entity.training;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.no.fullstack_project.entity.auth.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.operations.ModuleType;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;

/**
 * Represents a reusable training template that can be assigned to employees.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "training_templates")
public class TrainingTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "module_type", nullable = false)
  private ModuleType moduleType;

  @Column(length = 2000)
  private String description;

  @Column(name = "content_text", length = 4000)
  private String contentText;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TrainingCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "required_for_role", nullable = false)
  private ResponsibleRole requiredForRole;

  @Column(name = "is_mandatory", nullable = false)
  private boolean isMandatory;

  @Column(name = "validity_days")
  private Integer validityDays;

  @Column(name = "acknowledgment_required", nullable = false)
  private boolean acknowledgmentRequired;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
