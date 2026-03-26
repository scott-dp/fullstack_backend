package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.TrainingTemplate;

public interface TrainingTemplateRepository extends JpaRepository<TrainingTemplate, Long> {

  List<TrainingTemplate> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

  List<TrainingTemplate> findByOrganizationIdAndActiveTrue(Long organizationId);

  List<TrainingTemplate> findByOrganizationIdAndModuleTypeOrderByCreatedAtDesc(
      Long organizationId, ModuleType moduleType);
}
