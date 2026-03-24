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

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

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

  public List<NotificationResponse> listNotifications(Long userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public long getUnreadCount(Long userId) {
    return notificationRepository.countByUserIdAndReadFalse(userId);
  }

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

  @Transactional
  public void markAllAsRead(Long userId) {
    notificationRepository.markAllAsRead(userId);
    log.info("All notifications marked as read for user {}", userId);
  }

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
