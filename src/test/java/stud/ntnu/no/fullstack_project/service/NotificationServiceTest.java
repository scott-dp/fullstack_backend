package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.notification.NotificationResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Notification;
import stud.ntnu.no.fullstack_project.entity.NotificationType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  private AppUser testUser;
  private AppUser otherUser;

  @BeforeEach
  void setUp() {
    Organization testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);

    otherUser = new AppUser();
    otherUser.setId(2L);
    otherUser.setUsername("otheruser");
    otherUser.setPassword("encoded");
    otherUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private Notification buildNotification(Long id, AppUser user, String title,
      NotificationType type, boolean read) {
    Notification notification = new Notification();
    notification.setId(id);
    notification.setUser(user);
    notification.setTitle(title);
    notification.setMessage("Message for " + title);
    notification.setType(type);
    notification.setRead(read);
    notification.setReferenceId(100L);
    notification.setReferenceType("DEVIATION");
    notification.setCreatedAt(LocalDateTime.now());
    return notification;
  }

  // --- createNotification tests ---

  @Test
  void createNotification_savesNotification() {
    notificationService.createNotification(
        testUser,
        "Test Alert",
        "Something happened",
        NotificationType.GENERAL,
        50L,
        "CHECKLIST"
    );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertEquals(testUser, saved.getUser());
    assertEquals("Test Alert", saved.getTitle());
    assertEquals("Something happened", saved.getMessage());
    assertEquals(NotificationType.GENERAL, saved.getType());
    assertFalse(saved.isRead());
    assertEquals(50L, saved.getReferenceId());
    assertEquals("CHECKLIST", saved.getReferenceType());
  }

  @Test
  void createNotification_temperatureAlert_savesCorrectType() {
    notificationService.createNotification(
        testUser,
        "Critical Temperature",
        "Temperature at Fridge is 15.0C",
        NotificationType.TEMPERATURE_ALERT,
        10L,
        "TEMPERATURE_LOG"
    );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertEquals(NotificationType.TEMPERATURE_ALERT, captor.getValue().getType());
  }

  // --- listNotifications tests ---

  @Test
  void listNotifications_returnsUserNotifications() {
    Notification n1 = buildNotification(1L, testUser, "Alert 1",
        NotificationType.GENERAL, false);
    Notification n2 = buildNotification(2L, testUser, "Alert 2",
        NotificationType.DEVIATION_ASSIGNED, true);

    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(n1, n2));

    List<NotificationResponse> result = notificationService.listNotifications(1L);

    assertEquals(2, result.size());
    assertEquals("Alert 1", result.get(0).title());
    assertFalse(result.get(0).read());
    assertEquals("Alert 2", result.get(1).title());
    assertTrue(result.get(1).read());
    assertEquals("GENERAL", result.get(0).type());
    assertEquals("DEVIATION_ASSIGNED", result.get(1).type());
  }

  @Test
  void listNotifications_noNotifications_returnsEmpty() {
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of());

    List<NotificationResponse> result = notificationService.listNotifications(1L);

    assertTrue(result.isEmpty());
  }

  // --- getUnreadCount tests ---

  @Test
  void getUnreadCount_returnsCount() {
    when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(5L);

    long count = notificationService.getUnreadCount(1L);

    assertEquals(5L, count);
  }

  @Test
  void getUnreadCount_zeroUnread_returnsZero() {
    when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(0L);

    long count = notificationService.getUnreadCount(1L);

    assertEquals(0L, count);
  }

  // --- markAsRead tests ---

  @Test
  void markAsRead_marksNotificationAsRead() {
    Notification notification = buildNotification(1L, testUser, "Alert",
        NotificationType.GENERAL, false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    notificationService.markAsRead(1L, 1L);

    assertTrue(notification.isRead());
    verify(notificationRepository).save(notification);
  }

  @Test
  void markAsRead_notificationNotOwnedByUser_throws() {
    Notification notification = buildNotification(1L, otherUser, "Alert",
        NotificationType.GENERAL, false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> notificationService.markAsRead(1L, 1L));
    assertTrue(ex.getMessage().contains("does not belong"));
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void markAsRead_nonExistentNotification_throws() {
    when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> notificationService.markAsRead(999L, 1L));
  }

  // --- markAllAsRead tests ---

  @Test
  void markAllAsRead_delegatesToRepository() {
    notificationService.markAllAsRead(1L);

    verify(notificationRepository).markAllAsRead(1L);
  }
}
