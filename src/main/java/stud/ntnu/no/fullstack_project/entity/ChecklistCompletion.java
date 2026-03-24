package stud.ntnu.no.fullstack_project.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents a completed instance of a checklist template.
 * Contains the individual answers for each checklist item.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "checklist_completions")
public class ChecklistCompletion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private ChecklistTemplate template;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "completed_by", nullable = false)
  private AppUser completedBy;

  @CreationTimestamp
  @Column(name = "completed_at", updatable = false)
  private LocalDateTime completedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CompletionStatus status;

  private String comment;

  @OneToMany(mappedBy = "completion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChecklistAnswer> answers = new ArrayList<>();
}
