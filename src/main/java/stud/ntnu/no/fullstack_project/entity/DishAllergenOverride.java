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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Allows a dish to override the derived allergen status from its ingredients.
 *
 * <p>An override can either add an allergen that is not derived from ingredients
 * ({@code included = true}) or remove one that is ({@code included = false}).
 * A reason must always be provided for traceability.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dish_allergen_overrides")
public class DishAllergenOverride {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dish_id", nullable = false)
  private Dish dish;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "allergen_id", nullable = false)
  private Allergen allergen;

  @Column(nullable = false)
  private boolean included;

  @Column(nullable = false, length = 500)
  private String reason;
}
