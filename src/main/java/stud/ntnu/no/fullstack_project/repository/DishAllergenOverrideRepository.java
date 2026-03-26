package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.DishAllergenOverride;

public interface DishAllergenOverrideRepository extends JpaRepository<DishAllergenOverride, Long> {

  List<DishAllergenOverride> findByDishId(Long dishId);

  void deleteByDishId(Long dishId);
}
