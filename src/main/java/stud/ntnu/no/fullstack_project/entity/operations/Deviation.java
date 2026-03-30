package stud.ntnu.no.fullstack_project.entity.operations;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;

/**
 * Represents a non-compliance incident or deviation that needs tracking and resolution.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deviations")
public class Deviation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ComplianceCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeviationSeverity severity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeviationStatus status = DeviationStatus.OPEN;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reported_by", nullable = false)
  private AppUser reportedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to")
  private AppUser assignedTo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resolved_by")
  private AppUser resolvedBy;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "deviation", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<DeviationComment> comments = new ArrayList<>();
}
