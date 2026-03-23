package stud.ntnu.no.fullstack_project.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  boolean existsByUsername(String username);

  Optional<AppUser> findByUsername(String username);
}
