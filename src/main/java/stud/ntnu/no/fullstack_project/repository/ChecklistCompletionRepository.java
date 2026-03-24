package stud.ntnu.no.fullstack_project.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stud.ntnu.no.fullstack_project.entity.ChecklistCompletion;

public interface ChecklistCompletionRepository extends JpaRepository<ChecklistCompletion, Long> {

  List<ChecklistCompletion> findByTemplateOrganizationIdOrderByCompletedAtDesc(Long organizationId);

  List<ChecklistCompletion> findByTemplateIdOrderByCompletedAtDesc(Long templateId);

  @Query("SELECT c FROM ChecklistCompletion c WHERE c.template.organization.id = :orgId "
      + "AND c.completedAt >= :since ORDER BY c.completedAt DESC")
  List<ChecklistCompletion> findByOrganizationSince(
      @Param("orgId") Long organizationId,
      @Param("since") LocalDateTime since);

  long countByTemplateOrganizationIdAndCompletedAtAfter(Long organizationId, LocalDateTime after);
}
