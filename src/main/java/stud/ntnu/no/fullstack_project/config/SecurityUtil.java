package stud.ntnu.no.fullstack_project.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

  public String getCurrentUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetails userDetails) {
      return userDetails.getUsername();
    }

    throw new IllegalStateException("No authenticated user is available");
  }
}
