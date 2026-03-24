package stud.ntnu.no.fullstack_project.dto.checklist;

import jakarta.validation.constraints.NotNull;

public record ChecklistAnswerRequest(
    @NotNull Long itemId,
    boolean checked,
    String comment
) {}
