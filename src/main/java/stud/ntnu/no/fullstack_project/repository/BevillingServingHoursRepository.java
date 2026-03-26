package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.BevillingServingHours;

public interface BevillingServingHoursRepository extends JpaRepository<BevillingServingHours, Long> {

  List<BevillingServingHours> findByBevillingIdOrderByWeekdayAsc(Long bevillingId);

  void deleteByBevillingId(Long bevillingId);
}
