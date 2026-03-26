package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.Bevilling;
import stud.ntnu.no.fullstack_project.entity.BevillingStatus;

public interface BevillingRepository extends JpaRepository<Bevilling, Long> {

  List<Bevilling> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

  Optional<Bevilling> findByOrganizationIdAndStatus(Long organizationId, BevillingStatus status);

  List<Bevilling> findByOrganizationId(Long organizationId);
}
