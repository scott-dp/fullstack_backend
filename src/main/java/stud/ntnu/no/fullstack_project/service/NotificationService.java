package stud.ntnu.no.fullstack_project.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.notification.NotificationResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Notification;
import stud.ntnu.no.fullstack_project.entity.NotificationType;
import stud.ntnu.no.fullstack_project.repository.NotificationRepository;

/**
 * Service for managing user notifications.
 *
 * <p>Handles creation, listing, counting, and read-status management of
 * notifications for individual users.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /**
   * Creates and persists a new notification for the specified user.
   *
   * @param user          the recipient user
   * @param title         short title of the notification
   * @param message       detailed notification message
   * @param type          notification type
   * @param referenceId   ID of the referenced entity
   * @param referenceType type label of the referenced entity
   */
  @Transactional
  public void createNotification(AppUser user, String title, String message,
      NotificationType type, Long referenceId, String referenceType) {
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setTitle(title);
    notification.setMessage(message);
    notification.setType(type);
    notification.setRead(false);
    notification.setReferenceId(referenceId);
    notification.setReferenceType(referenceType);

    notificationRepository.save(notification);
    log.info("Notification created for user {}: {}", user.getUsername(), title);
  }

  /**
   * Lists all notifications for a user, ordered by most recent first.
   *
   * @param userId the user identifier
   * @return list of notification responses
   */
  public List<NotificationResponse> listNotifications(Long userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Returns the count of unread notifications for a user.
   *
   * @param userId the user identifier
   * @return number of unread notifications
   */
  public long getUnreadCount(Long userId) {
    return notificationRepository.countByUserIdAndReadFalse(userId);
  }

  /**
   * Marks a single notification as read, verifying it belongs to the specified user.
   *
   * @param notificationId the notification identifier
   * @param userId         the user identifier (for ownership verification)
   */
  @Transactional
  public void markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Notification not found with id: " + notificationId));

    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Notification does not belong to the current user");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
    log.info("Notification {} marked as read for user {}", notificationId, userId);
  }

  /**
   * Marks all notifications as read for the specified user.
   *
   * @param userId the user identifier
   */
  @Transactional
  public void markAllAsRead(Long userId) {
    notificationRepository.markAllAsRead(userId);
    log.info("All notifications marked as read for user {}", userId);
  }

  @Transactional
  public void deleteNotificationsForReferences(String referenceType, List<Long> referenceIds) {
    if (referenceIds == null || referenceIds.isEmpty()) {
      return;
    }
    notificationRepository.deleteByReferenceTypeAndReferenceIdIn(referenceType, referenceIds);
    log.info("Deleted notifications for referenceType={} count={}", referenceType,
        referenceIds.size());
  }

  /**
   * Maps a notification entity to its response DTO.
   *
   * @param notification the notification entity
   * @return the notification response DTO
   */
  private NotificationResponse mapToResponse(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getType().name(),
        notification.isRead(),
        notification.getReferenceId(),
        notification.getReferenceType(),
        notification.getCreatedAt()
    );
  }
}
