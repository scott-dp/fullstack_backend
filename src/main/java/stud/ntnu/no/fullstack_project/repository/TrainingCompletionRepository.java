package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.TrainingCompletion;

public interface TrainingCompletionRepository extends JpaRepository<TrainingCompletion, Long> {

  List<TrainingCompletion> findByTrainingAssignmentIdOrderByCompletedAtDesc(Long assignmentId);
}
