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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "routine_reviews")
public class RoutineReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routine_id", nullable = false)
  private Routine routine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by", nullable = false)
  private AppUser reviewedBy;

  @Column(name = "reviewed_at", nullable = false)
  private LocalDateTime reviewedAt;

  @Column(length = 2000)
  private String notes;

  @Column(name = "next_review_at")
  private LocalDateTime nextReviewAt;
}
