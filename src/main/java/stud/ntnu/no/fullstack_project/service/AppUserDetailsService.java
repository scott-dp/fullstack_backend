package stud.ntnu.no.fullstack_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  /**
   * Loads a user by username for Spring Security authentication and authorization.
   *
   * @param username username of the user to load
   * @return resolved user details implementation
   * @throws UsernameNotFoundException if no matching user exists
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Loading user details for username={}", username);
    return appUserRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.warn("User details lookup failed username={}", username);
          return new UsernameNotFoundException("User not found: " + username);
        });
  }
}
