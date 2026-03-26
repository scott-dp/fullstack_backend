package stud.ntnu.no.fullstack_project.entity;

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

/**
 * Single-use invitation token that allows an authenticated user to join an organization.
 *
 * <p>Invites are created by admins or restaurant managers and can be accepted once
 * by a logged-in user who does not already belong to an organization.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organization_invites")
public class OrganizationInvite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 120)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_to_assign", nullable = false)
  private Role roleToAssign;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by_user_id", nullable = false)
  private AppUser createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accepted_by_user_id")
  private AppUser acceptedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "accepted_at")
  private LocalDateTime acceptedAt;

  @Column(nullable = false)
  private boolean revoked;

  /**
   * Returns whether the invitation has expired.
   *
   * @return true when the expiration timestamp is in the past
   */
  public boolean isExpired() {
    return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
  }

  /**
   * Returns whether the invitation has already been used.
   *
   * @return true when accepted by a user
   */
  public boolean isAccepted() {
    return acceptedAt != null;
  }
}
