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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single line item within a delivery record.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "delivery_items")
public class DeliveryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_record_id", nullable = false)
  private DeliveryRecord deliveryRecord;

  @Column(name = "product_name", nullable = false)
  private String productName;

  private String quantity;

  private String unit;

  @Column(name = "batch_lot")
  private String batchLot;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Column(name = "internal_ingredient_ref")
  private String internalIngredientRef;
}
