package stud.ntnu.no.fullstack_project.dto.auth;

import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;

public record AuthStatusResponse(boolean authenticated, CurrentUserResponse user) {
}
