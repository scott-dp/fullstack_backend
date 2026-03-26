package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.RoutineReview;

public interface RoutineReviewRepository extends JpaRepository<RoutineReview, Long> {

  List<RoutineReview> findByRoutineIdOrderByReviewedAtDesc(Long routineId);

  void deleteByRoutineId(Long routineId);
}
