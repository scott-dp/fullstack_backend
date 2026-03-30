package stud.ntnu.no.fullstack_project.entity.food;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;

/**
 * Represents a dish served by an organization.
 *
 * <p>A dish is composed of ingredients (tracked via {@link DishIngredient}) and
 * inherits allergen information from those ingredients. Allergen overrides may
 * be applied at the dish level through {@link DishAllergenOverride}. Approval
 * tracking is provided via {@code lastApprovedAt}, {@code lastApprovedBy}, and
 * {@code allergenApprovalValid}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dishes")
public class Dish {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "last_approved_at")
  private LocalDateTime lastApprovedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_approved_by")
  private AppUser lastApprovedBy;

  @Column(name = "allergen_approval_valid", nullable = false)
  private boolean allergenApprovalValid;

  @Column(length = 2000)
  private String notes;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
