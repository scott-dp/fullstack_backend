package stud.ntnu.no.fullstack_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.config.SecurityUtil;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final AppUserRepository appUserRepository;
  private final SecurityUtil securityUtil;

  public CurrentUserResponse getCurrentUser() {
    AppUser user = appUserRepository.findByUsername(securityUtil.getCurrentUsername())
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
    return mapToResponse(user);
  }

  CurrentUserResponse mapToResponse(AppUser user) {
    return new CurrentUserResponse(
        user.getId(),
        user.getUsername(),
        user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
    );
  }
}
