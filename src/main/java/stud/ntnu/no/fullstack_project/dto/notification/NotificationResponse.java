package stud.ntnu.no.fullstack_project.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing a user notification.
 *
 * @param id            unique notification identifier
 * @param title         short title of the notification
 * @param message       detailed notification message
 * @param type          notification type (e.g. DEVIATION_ASSIGNED, TEMPERATURE_ALERT)
 * @param read          whether the notification has been read
 * @param referenceId   ID of the referenced entity (e.g. deviation or temperature log)
 * @param referenceType type of the referenced entity (e.g. DEVIATION, TEMPERATURE_LOG)
 * @param createdAt     timestamp when the notification was created
 */
@Schema(description = "Response representing a user notification.")
public record NotificationResponse(
    @Schema(description = "Unique notification identifier.", example = "1")
    Long id,

    @Schema(description = "Short title of the notification.", example = "Deviation Assigned")
    String title,

    @Schema(description = "Detailed notification message.", example = "You have been assigned to deviation: Fridge temperature too high")
    String message,

    @Schema(description = "Notification type.", example = "DEVIATION_ASSIGNED")
    String type,

    @Schema(description = "Whether the notification has been read.", example = "false")
    boolean read,

    @Schema(description = "ID of the referenced entity.", example = "1")
    Long referenceId,

    @Schema(description = "Type of the referenced entity.", example = "DEVIATION")
    String referenceType,

    @Schema(description = "Timestamp when the notification was created.", example = "2025-01-15T10:00:00")
    LocalDateTime createdAt
) {}
