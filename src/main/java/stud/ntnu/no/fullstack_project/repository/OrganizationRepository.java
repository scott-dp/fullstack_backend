package stud.ntnu.no.fullstack_project.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

  Optional<Organization> findByOrganizationNumber(String organizationNumber);

  boolean existsByOrganizationNumber(String organizationNumber);
}
