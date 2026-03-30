package stud.ntnu.no.fullstack_project.repository.training;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.training.TrainingAssignment;
import stud.ntnu.no.fullstack_project.entity.training.TrainingAssignmentStatus;

public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Long> {

  List<TrainingAssignment> findByAssigneeUserIdOrderByAssignedAtDesc(Long userId);

  List<TrainingAssignment> findByTrainingTemplateOrganizationIdOrderByAssignedAtDesc(
      Long organizationId);

  List<TrainingAssignment> findByAssigneeUserIdAndStatus(
      Long userId, TrainingAssignmentStatus status);

  List<TrainingAssignment> findByTrainingTemplateId(Long trainingTemplateId);

  long countByTrainingTemplateOrganizationIdAndStatus(
      Long organizationId, TrainingAssignmentStatus status);
}
