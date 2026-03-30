package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.licensing.AlcoholIncident;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentStatus;

public interface AlcoholIncidentRepository extends JpaRepository<AlcoholIncident, Long> {

  List<AlcoholIncident> findByOrganizationIdOrderByOccurredAtDesc(Long organizationId);

  List<AlcoholIncident> findByOrganizationIdAndStatus(Long organizationId, IncidentStatus status);

  long countByOrganizationIdAndStatus(Long organizationId, IncidentStatus status);
}
