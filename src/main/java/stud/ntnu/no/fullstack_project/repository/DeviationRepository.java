package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.ComplianceCategory;
import stud.ntnu.no.fullstack_project.entity.Deviation;
import stud.ntnu.no.fullstack_project.entity.DeviationStatus;

public interface DeviationRepository extends JpaRepository<Deviation, Long> {

  List<Deviation> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

  List<Deviation> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
      Long organizationId, DeviationStatus status);

  List<Deviation> findByOrganizationIdAndCategoryOrderByCreatedAtDesc(
      Long organizationId, ComplianceCategory category);

  List<Deviation> findByAssignedToIdOrderByCreatedAtDesc(Long userId);

  long countByOrganizationIdAndStatus(Long organizationId, DeviationStatus status);

  long countByOrganizationIdAndStatusIn(Long organizationId, List<DeviationStatus> statuses);
}
