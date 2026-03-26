package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Role;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

  boolean existsByUsername(String username);

  Optional<AppUser> findByUsername(String username);

  Optional<AppUser> findByEmail(String email);

  Optional<AppUser> findByUsernameOrEmail(String username, String email);

  List<AppUser> findByOrganizationId(Long organizationId);

  List<AppUser> findByOrganizationIdAndEnabledTrue(Long organizationId);

  boolean existsByEmail(String email);

  Optional<AppUser> findByEmailVerificationToken(String token);

  Optional<AppUser> findByAccountSetupToken(String token);

  boolean existsByRolesContaining(Role role);

  @Query("select distinct u from AppUser u join u.roles r where r = :role order by u.createdAt desc")
  List<AppUser> findAllByRole(@Param("role") Role role);
}
