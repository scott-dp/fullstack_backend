package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.DishIngredient;

public interface DishIngredientRepository extends JpaRepository<DishIngredient, Long> {

  List<DishIngredient> findByDishId(Long dishId);

  void deleteByDishId(Long dishId);
}
