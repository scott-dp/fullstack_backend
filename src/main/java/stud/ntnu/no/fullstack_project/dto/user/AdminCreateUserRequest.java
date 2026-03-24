package stud.ntnu.no.fullstack_project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AdminCreateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Size(min = 6, max = 255) String password,
    @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Email @Size(max = 255) String email,
    @NotNull Set<String> roles,
    @NotNull Long organizationId
) {}
