package stud.ntnu.no.fullstack_project.dto.deviation;

import java.time.LocalDateTime;
import java.util.List;

public record DeviationResponse(
    Long id,
    String title,
    String description,
    String category,
    String severity,
    String status,
    String reportedByUsername,
    String assignedToUsername,
    String resolvedByUsername,
    LocalDateTime resolvedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<DeviationCommentResponse> comments
) {}
