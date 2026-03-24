package stud.ntnu.no.fullstack_project.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility component for extracting the currently authenticated user's information
 * from the Spring Security context.
 */
@Component
public class SecurityUtil {

  /**
   * Returns the username of the currently authenticated user.
   *
   * @return the authenticated user's username
   * @throws IllegalStateException if no authenticated user is available
   */
  public String getCurrentUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetails userDetails) {
      return userDetails.getUsername();
    }

    throw new IllegalStateException("No authenticated user is available");
  }
}
