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
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;

/**
 * Represents the assignment of a training template to a specific user.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "training_assignments")
public class TrainingAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_template_id", nullable = false)
  private TrainingTemplate trainingTemplate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignee_user_id", nullable = false)
  private AppUser assigneeUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_by", nullable = false)
  private AppUser assignedBy;

  @CreationTimestamp
  @Column(name = "assigned_at", updatable = false)
  private LocalDateTime assignedAt;

  @Column(name = "due_at")
  private LocalDateTime dueAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TrainingAssignmentStatus status = TrainingAssignmentStatus.ASSIGNED;
}
