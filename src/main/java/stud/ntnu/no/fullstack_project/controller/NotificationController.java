package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.notification.NotificationResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(summary = "List notifications for the current user")
  public ResponseEntity<List<NotificationResponse>> listNotifications(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(notificationService.listNotifications(currentUser.getId()));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "Get the unread notification count for the current user")
  public ResponseEntity<Long> getUnreadCount(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(notificationService.getUnreadCount(currentUser.getId()));
  }

  @PutMapping("/{id}/read")
  @Operation(summary = "Mark a notification as read")
  public ResponseEntity<Void> markAsRead(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    notificationService.markAsRead(id, currentUser.getId());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/read-all")
  @Operation(summary = "Mark all notifications as read for the current user")
  public ResponseEntity<Void> markAllAsRead(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    notificationService.markAllAsRead(currentUser.getId());
    return ResponseEntity.noContent().build();
  }
}
