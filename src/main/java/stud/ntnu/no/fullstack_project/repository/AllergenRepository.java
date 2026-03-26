package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.Allergen;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {

  List<Allergen> findByActiveTrue();

  Optional<Allergen> findByCode(String code);
}
