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
import stud.ntnu.no.fullstack_project.entity.organization.Organization;

/**
 * Represents a product recall case linked to a supplier and/or batch.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "recall_cases")
public class RecallCase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String title;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @Column(name = "product_name")
  private String productName;

  @Column(name = "batch_lot")
  private String batchLot;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(name = "opened_at", nullable = false)
  private LocalDateTime openedAt;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RecallStatus status = RecallStatus.OPEN;
}
