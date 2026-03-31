package stud.ntnu.no.fullstack_project.entity.licensing;

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
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stud.ntnu.no.fullstack_project.entity.operations.Weekday;

/**
 * Defines allowed serving hours for a specific day of the week on a bevilling.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bevilling_serving_hours")
public class BevillingServingHours {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bevilling_id", nullable = false)
  private Bevilling bevilling;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Weekday weekday;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "consumption_deadline_minutes_after_end", nullable = false)
  private int consumptionDeadlineMinutesAfterEnd = 30;
}
