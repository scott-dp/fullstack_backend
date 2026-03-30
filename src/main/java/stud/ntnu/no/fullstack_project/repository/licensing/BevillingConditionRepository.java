package stud.ntnu.no.fullstack_project.repository.licensing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.licensing.BevillingCondition;

public interface BevillingConditionRepository extends JpaRepository<BevillingCondition, Long> {

  List<BevillingCondition> findByBevillingIdOrderByIdAsc(Long bevillingId);
}
