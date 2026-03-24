package stud.ntnu.no.fullstack_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

/**
 * Spring Security {@link UserDetailsService} implementation backed by the application's
 * user repository.
 *
 * <p>Loaded by the authentication provider to resolve a username into a fully
 * populated {@link UserDetails} instance during authentication.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  /**
   * Loads a user by their username for Spring Security authentication.
   *
   * @param username the username to look up
   * @return the user details
   * @throws UsernameNotFoundException if no user exists with the given username
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Loading user details for username={}", username);
    return appUserRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
