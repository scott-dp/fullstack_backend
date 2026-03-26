package stud.ntnu.no.fullstack_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Represents a food ingredient within an organization's inventory.
 *
 * <p>Each ingredient belongs to one organization and may be associated with
 * one or more allergens through a many-to-many relationship.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredients")
public class Ingredient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String name;

  @Column(length = 2000)
  private String notes;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "ingredient_allergens",
      joinColumns = @JoinColumn(name = "ingredient_id"),
      inverseJoinColumns = @JoinColumn(name = "allergen_id")
  )
  private Set<Allergen> allergens = new HashSet<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
