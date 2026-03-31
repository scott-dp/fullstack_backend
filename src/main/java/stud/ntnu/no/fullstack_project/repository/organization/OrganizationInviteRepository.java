package stud.ntnu.no.fullstack_project.repository.organization;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationInvite;

/**
 * Repository for organization invitation tokens.
 */
public interface OrganizationInviteRepository extends JpaRepository<OrganizationInvite, Long> {

  Optional<OrganizationInvite> findByToken(String token);

  List<OrganizationInvite> findAllByOrderByCreatedAtDesc();

  List<OrganizationInvite> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
