package stud.ntnu.no.fullstack_project.entity.licensing;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;

/**
 * Represents a bevilling (alcohol license) for an organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bevillinger")
public class Bevilling {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String municipality;

  @Enumerated(EnumType.STRING)
  @Column(name = "bevilling_type", nullable = false)
  private BevillingType bevillingType;

  @Column(name = "valid_from", nullable = false)
  private LocalDate validFrom;

  @Column(name = "valid_to")
  private LocalDate validTo;

  @Column(name = "license_number")
  private String licenseNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BevillingStatus status = BevillingStatus.ACTIVE;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "bevilling_alcohol_groups", joinColumns = @JoinColumn(name = "bevilling_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "alcohol_group")
  private Set<AlcoholGroup> alcoholGroupsAllowed = new HashSet<>();

  @Column(name = "serving_area_description", length = 2000)
  private String servingAreaDescription;

  @Column(name = "indoor_allowed", nullable = false)
  private boolean indoorAllowed = true;

  @Column(name = "outdoor_allowed", nullable = false)
  private boolean outdoorAllowed = false;

  @Column(name = "styrer_name")
  private String styrerName;

  @Column(name = "stedfortreder_name")
  private String stedfortrederName;

  @Column(length = 2000)
  private String notes;

  @Column(name = "attachment_id")
  private Long attachmentId;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
