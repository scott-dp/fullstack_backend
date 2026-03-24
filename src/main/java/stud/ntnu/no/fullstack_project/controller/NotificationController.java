package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.notification.NotificationResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.NotificationService;

/**
 * REST controller for managing user notifications.
 *
 * <p>Provides endpoints to list, count, and mark notifications as read for
 * the currently authenticated user.</p>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Endpoints for user notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(
      summary = "List notifications for the current user",
      description = "Returns all notifications for the authenticated user, ordered by most recent first."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
  })
  public ResponseEntity<List<NotificationResponse>> listNotifications(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing notifications for user={}", currentUser.getUsername());
    return ResponseEntity.ok(notificationService.listNotifications(currentUser.getId()));
  }

  @GetMapping("/unread-count")
  @Operation(
      summary = "Get the unread notification count for the current user",
      description = "Returns the total number of unread notifications."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully",
          content = @Content(schema = @Schema(implementation = Long.class)))
  })
  public ResponseEntity<Long> getUnreadCount(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Fetching unread notification count for user={}", currentUser.getUsername());
    return ResponseEntity.ok(notificationService.getUnreadCount(currentUser.getId()));
  }

  @PutMapping("/{id}/read")
  @Operation(
      summary = "Mark a notification as read",
      description = "Sets the read flag on a single notification owned by the authenticated user."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Notification marked as read"),
      @ApiResponse(responseCode = "400", description = "Notification not found or does not belong to user",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<Void> markAsRead(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Marking notification id={} as read for user={}", id, currentUser.getUsername());
    notificationService.markAsRead(id, currentUser.getId());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/read-all")
  @Operation(
      summary = "Mark all notifications as read for the current user",
      description = "Bulk-updates all unread notifications for the authenticated user to read."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All notifications marked as read")
  })
  public ResponseEntity<Void> markAllAsRead(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Marking all notifications as read for user={}", currentUser.getUsername());
    notificationService.markAllAsRead(currentUser.getId());
    return ResponseEntity.noContent().build();
  }
}
