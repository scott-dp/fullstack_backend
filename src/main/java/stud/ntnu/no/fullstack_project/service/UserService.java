package stud.ntnu.no.fullstack_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.config.SecurityUtil;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final AppUserRepository appUserRepository;
  private final SecurityUtil securityUtil;

  /**
   * Retrieves the currently authenticated user as a DTO.
   *
   * @return current user response for the authenticated principal
   */
  public CurrentUserResponse getCurrentUser() {
    String username = securityUtil.getCurrentUsername();
    log.info("Loading current user username={}", username);

    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
    log.debug("Loaded current user id={} username={} roles={}", user.getId(), user.getUsername(), user.getRoles());
    return mapToResponse(user);
  }

  /**
   * Maps the persistence entity to the public user response DTO.
   *
   * @param user entity to map
   * @return mapped user response
   */
  CurrentUserResponse mapToResponse(AppUser user) {
    return new CurrentUserResponse(
        user.getId(),
        user.getUsername(),
        user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
    );
  }
}
