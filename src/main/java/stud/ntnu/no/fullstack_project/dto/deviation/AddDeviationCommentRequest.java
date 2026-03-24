package stud.ntnu.no.fullstack_project.dto.deviation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddDeviationCommentRequest(
    @NotBlank @Size(max = 2000) String content
) {}
