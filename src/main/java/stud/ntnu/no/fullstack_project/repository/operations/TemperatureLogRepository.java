package stud.ntnu.no.fullstack_project.repository.operations;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureLog;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;

public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, Long> {

  List<TemperatureLog> findByOrganizationIdOrderByRecordedAtDesc(Long organizationId);

  List<TemperatureLog> findByOrganizationIdAndRecordedAtAfterOrderByRecordedAtDesc(
      Long organizationId, LocalDateTime after);

  List<TemperatureLog> findByOrganizationIdAndLocationOrderByRecordedAtDesc(
      Long organizationId, String location);

  long countByOrganizationIdAndStatusAndRecordedAtAfter(
      Long organizationId, TemperatureStatus status, LocalDateTime after);
}
