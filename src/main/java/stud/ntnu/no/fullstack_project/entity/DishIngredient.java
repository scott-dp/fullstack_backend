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
 * Join entity linking a {@link Dish} to an {@link Ingredient} with an optional
 * quantity description.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dish_ingredients")
public class DishIngredient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dish_id", nullable = false)
  private Dish dish;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(name = "quantity_text")
  private String quantityText;
}
