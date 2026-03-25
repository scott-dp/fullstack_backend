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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A condition or requirement attached to a bevilling (alcohol license).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bevilling_conditions")
public class BevillingCondition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bevilling_id", nullable = false)
  private Bevilling bevilling;

  @Enumerated(EnumType.STRING)
  @Column(name = "condition_type", nullable = false)
  private ConditionType conditionType;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private boolean active = true;
}
