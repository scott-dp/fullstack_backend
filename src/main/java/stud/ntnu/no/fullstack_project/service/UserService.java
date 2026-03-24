package stud.ntnu.no.fullstack_project.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.config.SecurityUtil;
import stud.ntnu.no.fullstack_project.dto.user.AdminCreateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.dto.user.UpdateUserRequest;
import stud.ntnu.no.fullstack_project.dto.user.UserSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final AppUserRepository appUserRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecurityUtil securityUtil;

  public CurrentUserResponse getCurrentUser() {
    AppUser user = appUserRepository.findByUsername(securityUtil.getCurrentUsername())
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
    return mapToResponse(user);
  }

  @Transactional
  public CurrentUserResponse updateProfile(UpdateUserRequest request, AppUser currentUser) {
    if (request.email() != null && !request.email().equals(currentUser.getEmail())
        && appUserRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    if (request.firstName() != null) {
      currentUser.setFirstName(request.firstName());
    }
    if (request.lastName() != null) {
      currentUser.setLastName(request.lastName());
    }
    if (request.email() != null) {
      currentUser.setEmail(request.email());
    }

    AppUser saved = appUserRepository.save(currentUser);
    log.info("User profile updated for user: {}", saved.getUsername());
    return mapToResponse(saved);
  }

  public List<UserSummaryResponse> listUsersInOrganization(Long organizationId) {
    return appUserRepository.findByOrganizationId(organizationId).stream()
        .map(this::mapToSummary)
        .collect(Collectors.toList());
  }

  @Transactional
  public CurrentUserResponse createUser(AdminCreateUserRequest request) {
    if (appUserRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username is already taken");
    }
    if (request.email() != null && appUserRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    Organization org = organizationRepository.findById(request.organizationId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Organization not found with id: " + request.organizationId()));

    Set<Role> roles = request.roles().stream()
        .map(r -> {
          try {
            return Role.valueOf(r);
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + r);
          }
        })
        .collect(Collectors.toSet());

    AppUser user = new AppUser();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setOrganization(org);
    user.setRoles(roles);

    AppUser saved = appUserRepository.save(user);
    log.info("User created by admin: {} (id={})", saved.getUsername(), saved.getId());
    return mapToResponse(saved);
  }

  CurrentUserResponse mapToResponse(AppUser user) {
    Organization org = user.getOrganization();
    return new CurrentUserResponse(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
        org != null ? org.getId() : null,
        org != null ? org.getName() : null
    );
  }

  private UserSummaryResponse mapToSummary(AppUser user) {
    return new UserSummaryResponse(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
    );
  }
}
