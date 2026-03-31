package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.ChecklistTemplate;
import stud.ntnu.no.fullstack_project.entity.operations.ComplianceCategory;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

  List<ChecklistTemplate> findByOrganizationIdAndActiveTrue(Long organizationId);

  List<ChecklistTemplate> findByOrganizationIdAndCategoryAndActiveTrue(
      Long organizationId, ComplianceCategory category);

  List<ChecklistTemplate> findByOrganizationId(Long organizationId);
}
