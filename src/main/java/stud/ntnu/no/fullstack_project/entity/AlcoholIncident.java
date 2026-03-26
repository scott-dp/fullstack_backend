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

/**
 * Represents an alcohol-related incident or refusal that requires logging and follow-up.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alcohol_incidents")
public class AlcoholIncident {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reported_by", nullable = false)
  private AppUser reportedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to")
  private AppUser assignedTo;

  @Column(name = "shift_label")
  private String shiftLabel;

  @Column(name = "location_area")
  private String locationArea;

  @Enumerated(EnumType.STRING)
  @Column(name = "incident_type", nullable = false)
  private IncidentType incidentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IncidentSeverity severity;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(name = "immediate_action_taken", length = 2000)
  private String immediateActionTaken;

  @Column(name = "follow_up_required", nullable = false)
  private boolean followUpRequired;

  @Column(name = "linked_routine_id")
  private Long linkedRoutineId;

  @Column(name = "linked_deviation_id")
  private Long linkedDeviationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IncidentStatus status = IncidentStatus.OPEN;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "closed_by")
  private AppUser closedBy;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
