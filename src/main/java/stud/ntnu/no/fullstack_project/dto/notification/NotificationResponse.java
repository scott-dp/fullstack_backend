package stud.ntnu.no.fullstack_project.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String type,
    boolean read,
    Long referenceId,
    String referenceType,
    LocalDateTime createdAt
) {}
