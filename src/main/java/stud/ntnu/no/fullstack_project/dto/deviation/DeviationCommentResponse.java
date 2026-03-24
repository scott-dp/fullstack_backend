package stud.ntnu.no.fullstack_project.dto.deviation;

import java.time.LocalDateTime;

public record DeviationCommentResponse(
    Long id,
    String authorUsername,
    String content,
    LocalDateTime createdAt
) {}
