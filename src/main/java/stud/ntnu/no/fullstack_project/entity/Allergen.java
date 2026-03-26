package stud.ntnu.no.fullstack_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reference entity representing one of the 14 EU-defined food allergens.
 *
 * <p>Allergen records are seeded on first run and are not expected to change
 * during normal application usage. Each allergen has a unique code, a Norwegian
 * name, and an English name.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "allergens")
public class Allergen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(name = "name_no", nullable = false)
  private String nameNo;

  @Column(name = "name_en", nullable = false)
  private String nameEn;

  @Column(nullable = false)
  private boolean active = true;
}
