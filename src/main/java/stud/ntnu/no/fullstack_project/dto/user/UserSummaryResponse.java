package stud.ntnu.no.fullstack_project.dto.user;

import java.util.Set;

public record UserSummaryResponse(
    Long id,
    String username,
    String firstName,
    String lastName,
    Set<String> roles
) {}
