package stud.ntnu.no.fullstack_project.dto.checklist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CompleteChecklistRequest(
    @NotNull Long templateId,
    @NotEmpty @Valid List<ChecklistAnswerRequest> answers,
    String comment
) {}
