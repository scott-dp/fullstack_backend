package stud.ntnu.no.fullstack_project.entity;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "routines")
public class Routine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "module_type", nullable = false)
  private ModuleType moduleType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoutineCategory category;

  @Column(length = 2000)
  private String description;

  @Column(length = 1000)
  private String purpose;

  @Enumerated(EnumType.STRING)
  @Column(name = "responsible_role", nullable = false)
  private ResponsibleRole responsibleRole;

  @Enumerated(EnumType.STRING)
  @Column(name = "frequency_type", nullable = false)
  private FrequencyType frequencyType;

  @Column(name = "steps_text", length = 4000)
  private String stepsText;

  @Column(name = "what_is_deviation_text", length = 2000)
  private String whatIsDeviationText;

  @Column(name = "corrective_action_text", length = 2000)
  private String correctiveActionText;

  @Column(name = "required_evidence_text", length = 2000)
  private String requiredEvidenceText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "linked_checklist_template_id")
  private ChecklistTemplate linkedChecklistTemplate;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "review_interval_days")
  private Integer reviewIntervalDays;

  @Column(name = "last_reviewed_at")
  private LocalDateTime lastReviewedAt;

  @Column(name = "version_number", nullable = false)
  private int versionNumber = 1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private AppUser createdBy;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
