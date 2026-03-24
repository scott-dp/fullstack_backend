package stud.ntnu.no.fullstack_project.dto.checklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChecklistItemRequest(
    @NotBlank @Size(max = 500) String description,
    int sortOrder,
    boolean requiresComment
) {}
