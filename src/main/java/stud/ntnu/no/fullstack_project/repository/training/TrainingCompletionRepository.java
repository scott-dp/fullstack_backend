package stud.ntnu.no.fullstack_project.repository.training;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.training.TrainingCompletion;

public interface TrainingCompletionRepository extends JpaRepository<TrainingCompletion, Long> {

  List<TrainingCompletion> findByTrainingAssignmentIdOrderByCompletedAtDesc(Long assignmentId);

  void deleteByTrainingAssignmentIdIn(List<Long> assignmentIds);
}
