package stud.ntnu.no.fullstack_project.dto.user;

import java.util.Set;

public record CurrentUserResponse(Long id, String username, Set<String> roles) {
}
