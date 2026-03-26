package stud.ntnu.no.fullstack_project.entity;

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

/**
 * Records the completion of a training assignment by an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "training_completions")
public class TrainingCompletion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_assignment_id", nullable = false)
  private TrainingAssignment trainingAssignment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "completed_by_user_id", nullable = false)
  private AppUser completedByUser;

  @CreationTimestamp
  @Column(name = "completed_at", updatable = false)
  private LocalDateTime completedAt;

  @Column(name = "acknowledgement_checked", nullable = false)
  private boolean acknowledgementChecked;

  @Column(length = 2000)
  private String comments;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;
}
