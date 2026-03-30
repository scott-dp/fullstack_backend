package stud.ntnu.no.fullstack_project.repository.food;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.food.Dish;

public interface DishRepository extends JpaRepository<Dish, Long> {

  List<Dish> findByOrganizationIdOrderByNameAsc(Long organizationId);

  List<Dish> findByOrganizationIdAndActiveTrue(Long organizationId);
}
