package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.TrainingAssignment;
import stud.ntnu.no.fullstack_project.entity.TrainingAssignmentStatus;

public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Long> {

  List<TrainingAssignment> findByAssigneeUserIdOrderByAssignedAtDesc(Long userId);

  List<TrainingAssignment> findByTrainingTemplateOrganizationIdOrderByAssignedAtDesc(
      Long organizationId);

  List<TrainingAssignment> findByAssigneeUserIdAndStatus(
      Long userId, TrainingAssignmentStatus status);

  long countByTrainingTemplateOrganizationIdAndStatus(
      Long organizationId, TrainingAssignmentStatus status);
}
