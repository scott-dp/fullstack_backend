package stud.ntnu.no.fullstack_project.dto.checklist;

import java.time.LocalDateTime;
import java.util.List;

public record ChecklistTemplateResponse(
    Long id,
    String title,
    String description,
    String frequency,
    String category,
    boolean active,
    List<ChecklistItemResponse> items,
    String createdByUsername,
    LocalDateTime createdAt
) {}
