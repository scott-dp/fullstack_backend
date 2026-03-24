package stud.ntnu.no.fullstack_project.dto.checklist;

import java.time.LocalDateTime;
import java.util.List;

public record ChecklistCompletionResponse(
    Long id,
    Long templateId,
    String templateTitle,
    String completedByUsername,
    LocalDateTime completedAt,
    String status,
    String comment,
    List<ChecklistAnswerResponse> answers
) {}
