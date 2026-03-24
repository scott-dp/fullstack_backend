package stud.ntnu.no.fullstack_project.dto.checklist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChecklistTemplateRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 1000) String description,
    @NotNull String frequency,
    @NotNull String category,
    @NotEmpty @Valid List<CreateChecklistItemRequest> items
) {}
