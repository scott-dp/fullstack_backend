package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.RecallCase;

public interface RecallCaseRepository extends JpaRepository<RecallCase, Long> {

  List<RecallCase> findByOrganizationIdOrderByOpenedAtDesc(Long organizationId);
}
