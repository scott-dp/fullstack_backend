package stud.ntnu.no.fullstack_project.dto.checklist;

public record ChecklistAnswerResponse(
    Long id,
    Long itemId,
    String itemDescription,
    boolean checked,
    String comment
) {}
