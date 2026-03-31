package stud.ntnu.no.fullstack_project.entity.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;

/**
 * User notification for task reminders, deviation assignments, and temperature alerts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 1000)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(name = "reference_id")
  private Long referenceId;

  @Column(name = "reference_type")
  private String referenceType;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
