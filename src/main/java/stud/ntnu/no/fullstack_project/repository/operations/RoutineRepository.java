package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.ModuleType;
import stud.ntnu.no.fullstack_project.entity.operations.Routine;
import stud.ntnu.no.fullstack_project.entity.operations.RoutineCategory;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

  List<Routine> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

  List<Routine> findByOrganizationIdAndActiveTrueOrderByCreatedAtDesc(Long organizationId);

  List<Routine> findByOrganizationIdAndModuleTypeOrderByCreatedAtDesc(
      Long organizationId, ModuleType moduleType);

  List<Routine> findByOrganizationIdAndCategoryOrderByCreatedAtDesc(
      Long organizationId, RoutineCategory category);

  List<Routine> findByOrganizationIdAndActiveTrue(Long organizationId);

  long countByOrganizationIdAndActiveTrue(Long organizationId);
}
