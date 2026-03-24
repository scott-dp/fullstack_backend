package stud.ntnu.no.fullstack_project.dto.checklist;

public record ChecklistItemResponse(
    Long id,
    String description,
    int sortOrder,
    boolean requiresComment
) {}
