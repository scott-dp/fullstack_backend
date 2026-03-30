package stud.ntnu.no.fullstack_project.repository.food;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.food.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

  List<Ingredient> findByOrganizationIdOrderByNameAsc(Long organizationId);
}
