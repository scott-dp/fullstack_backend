package stud.ntnu.no.fullstack_project.entity.operations;

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
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;

/**
 * Records a temperature measurement at a specific storage location.
 * Automatically determines status based on defined thresholds.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "temperature_logs")
public class TemperatureLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String location;

  @Column(nullable = false)
  private double temperature;

  @Column(name = "min_threshold", nullable = false)
  private double minThreshold;

  @Column(name = "max_threshold", nullable = false)
  private double maxThreshold;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TemperatureStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by", nullable = false)
  private AppUser recordedBy;

  @CreationTimestamp
  @Column(name = "recorded_at", updatable = false)
  private LocalDateTime recordedAt;

  private String comment;
}
