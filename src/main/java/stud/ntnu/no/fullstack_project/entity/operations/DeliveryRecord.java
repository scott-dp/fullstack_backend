package stud.ntnu.no.fullstack_project.entity.operations;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;

/**
 * Records a delivery received from a supplier, including line items.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "delivery_records")
public class DeliveryRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", nullable = false)
  private Supplier supplier;

  @Column(name = "delivery_date", nullable = false)
  private LocalDate deliveryDate;

  @Column(name = "document_number")
  private String documentNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "received_by", nullable = false)
  private AppUser receivedBy;

  @Column(length = 2000)
  private String notes;

  @OneToMany(mappedBy = "deliveryRecord", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeliveryItem> items = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
