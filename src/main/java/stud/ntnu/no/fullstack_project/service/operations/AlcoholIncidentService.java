package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.incident.AlcoholIncidentResponse;
import stud.ntnu.no.fullstack_project.dto.incident.CloseIncidentRequest;
import stud.ntnu.no.fullstack_project.dto.incident.CreateAlcoholIncidentRequest;
import stud.ntnu.no.fullstack_project.dto.incident.IncidentReportResponse;
import stud.ntnu.no.fullstack_project.dto.incident.UpdateAlcoholIncidentRequest;
import stud.ntnu.no.fullstack_project.entity.licensing.AlcoholIncident;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentSeverity;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentStatus;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentType;
import stud.ntnu.no.fullstack_project.repository.operations.AlcoholIncidentRepository;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;

/**
 * Service for managing alcohol incident and refusal log entries.
 *
 * <p>Handles creation, retrieval, filtering, updates, closure, and
 * reporting of alcohol-related incidents within an organization.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlcoholIncidentService {

  private final AlcoholIncidentRepository alcoholIncidentRepository;
  private final AppUserRepository appUserRepository;

  /**
   * Creates a new alcohol incident and persists it.
   *
   * @param request     the incident details
   * @param currentUser the authenticated user reporting the incident
   * @return the created incident response
   */
  @Transactional
  public AlcoholIncidentResponse create(CreateAlcoholIncidentRequest request,
      AppUser currentUser) {
    IncidentType type;
    try {
      type = IncidentType.valueOf(request.incidentType());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid incident type: " + request.incidentType());
    }

    IncidentSeverity severity;
    try {
      severity = IncidentSeverity.valueOf(request.severity());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid severity: " + request.severity());
    }

    AlcoholIncident incident = new AlcoholIncident();
    incident.setOrganization(currentUser.getOrganization());
    incident.setOccurredAt(LocalDateTime.parse(request.occurredAt()));
    incident.setReportedBy(currentUser);
    incident.setShiftLabel(request.shiftLabel());
    incident.setLocationArea(request.locationArea());
    incident.setIncidentType(type);
    incident.setSeverity(severity);
    incident.setDescription(request.description());
    incident.setImmediateActionTaken(request.immediateActionTaken());
    incident.setFollowUpRequired(request.followUpRequired());
    incident.setLinkedRoutineId(request.linkedRoutineId());
    incident.setLinkedDeviationId(request.linkedDeviationId());
    incident.setStatus(IncidentStatus.OPEN);

    if (request.assignedToId() != null) {
      AppUser assignee = appUserRepository.findById(request.assignedToId())
          .orElseThrow(() -> new IllegalArgumentException(
              "User not found with id: " + request.assignedToId()));
      incident.setAssignedTo(assignee);
    }

    AlcoholIncident saved = alcoholIncidentRepository.save(incident);
    log.info("Alcohol incident created: id={}, type={}", saved.getId(), saved.getIncidentType());
    return mapToResponse(saved);
  }

  /**
   * Retrieves an alcohol incident by its ID.
   *
   * @param id the incident identifier
   * @return the incident response
   */
  public AlcoholIncidentResponse get(Long id) {
    AlcoholIncident incident = alcoholIncidentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Alcohol incident not found with id: " + id));
    return mapToResponse(incident);
  }

  /**
   * Lists alcohol incidents for an organization, optionally filtered by status or type.
   *
   * @param organizationId the organization identifier
   * @param status         optional status filter
   * @param type           optional incident type filter
   * @return list of matching incident responses
   */
  public List<AlcoholIncidentResponse> list(Long organizationId, String status, String type) {
    List<AlcoholIncident> incidents;

    if (status != null && !status.isBlank()) {
      IncidentStatus incidentStatus;
      try {
        incidentStatus = IncidentStatus.valueOf(status);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + status);
      }
      incidents = alcoholIncidentRepository.findByOrganizationIdAndStatus(
          organizationId, incidentStatus);
    } else {
      incidents = alcoholIncidentRepository.findByOrganizationIdOrderByOccurredAtDesc(
          organizationId);
    }

    if (type != null && !type.isBlank()) {
      IncidentType incidentType;
      try {
        incidentType = IncidentType.valueOf(type);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid incident type: " + type);
      }
      incidents = incidents.stream()
          .filter(i -> i.getIncidentType() == incidentType)
          .collect(Collectors.toList());
    }

    return incidents.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an alcohol incident's fields.
   *
   * @param id          the incident identifier
   * @param request     the fields to update
   * @param currentUser the authenticated user performing the update
   * @return the updated incident response
   */
  @Transactional
  public AlcoholIncidentResponse update(Long id, UpdateAlcoholIncidentRequest request,
      AppUser currentUser) {
    AlcoholIncident incident = alcoholIncidentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Alcohol incident not found with id: " + id));

    if (request.severity() != null && !request.severity().isBlank()) {
      IncidentSeverity severity;
      try {
        severity = IncidentSeverity.valueOf(request.severity());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid severity: " + request.severity());
      }
      incident.setSeverity(severity);
    }

    if (request.description() != null && !request.description().isBlank()) {
      incident.setDescription(request.description());
    }

    if (request.immediateActionTaken() != null) {
      incident.setImmediateActionTaken(request.immediateActionTaken());
    }

    if (request.followUpRequired() != null) {
      incident.setFollowUpRequired(request.followUpRequired());
    }

    if (request.status() != null && !request.status().isBlank()) {
      IncidentStatus newStatus;
      try {
        newStatus = IncidentStatus.valueOf(request.status());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + request.status());
      }
      incident.setStatus(newStatus);
    }

    if (request.assignedToId() != null) {
      AppUser assignee = appUserRepository.findById(request.assignedToId())
          .orElseThrow(() -> new IllegalArgumentException(
              "User not found with id: " + request.assignedToId()));
      incident.setAssignedTo(assignee);
    }

    AlcoholIncident saved = alcoholIncidentRepository.save(incident);
    log.info("Alcohol incident updated: id={}, status={}", saved.getId(), saved.getStatus());
    return mapToResponse(saved);
  }

  /**
   * Closes an alcohol incident.
   *
   * @param id          the incident identifier
   * @param request     optional closing notes
   * @param currentUser the authenticated user closing the incident
   * @return the closed incident response
   */
  @Transactional
  public AlcoholIncidentResponse close(Long id, CloseIncidentRequest request,
      AppUser currentUser) {
    AlcoholIncident incident = alcoholIncidentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Alcohol incident not found with id: " + id));

    incident.setStatus(IncidentStatus.CLOSED);
    incident.setClosedAt(LocalDateTime.now());
    incident.setClosedBy(currentUser);

    if (request.notes() != null && !request.notes().isBlank()) {
      String existing = incident.getImmediateActionTaken();
      String closingNote = "[Closing notes] " + request.notes();
      incident.setImmediateActionTaken(
          existing != null && !existing.isBlank()
              ? existing + "\n" + closingNote
              : closingNote
      );
    }

    AlcoholIncident saved = alcoholIncidentRepository.save(incident);
    log.info("Alcohol incident closed: id={}, closedBy={}", saved.getId(),
        currentUser.getUsername());
    return mapToResponse(saved);
  }

  /**
   * Generates a summary report of alcohol incidents for an organization.
   *
   * @param organizationId the organization identifier
   * @return the incident report
   */
  public IncidentReportResponse report(Long organizationId) {
    List<AlcoholIncident> all = alcoholIncidentRepository
        .findByOrganizationIdOrderByOccurredAtDesc(organizationId);

    long totalIncidents = all.size();
    long openCount = alcoholIncidentRepository.countByOrganizationIdAndStatus(
        organizationId, IncidentStatus.OPEN);
    long closedCount = alcoholIncidentRepository.countByOrganizationIdAndStatus(
        organizationId, IncidentStatus.CLOSED);

    Map<String, Long> byType = all.stream()
        .collect(Collectors.groupingBy(
            i -> i.getIncidentType().name(),
            Collectors.counting()
        ));

    log.info("Incident report generated for orgId={}: total={}", organizationId, totalIncidents);
    return new IncidentReportResponse(totalIncidents, openCount, closedCount, byType);
  }

  /**
   * Maps an alcohol incident entity to its response DTO.
   *
   * @param incident the incident entity
   * @return the incident response DTO
   */
  private AlcoholIncidentResponse mapToResponse(AlcoholIncident incident) {
    return new AlcoholIncidentResponse(
        incident.getId(),
        incident.getOccurredAt(),
        incident.getReportedBy().getUsername(),
        incident.getAssignedTo() != null ? incident.getAssignedTo().getId() : null,
        incident.getAssignedTo() != null ? incident.getAssignedTo().getUsername() : null,
        incident.getShiftLabel(),
        incident.getLocationArea(),
        incident.getIncidentType().name(),
        incident.getSeverity().name(),
        incident.getDescription(),
        incident.getImmediateActionTaken(),
        incident.isFollowUpRequired(),
        incident.getLinkedRoutineId(),
        incident.getLinkedDeviationId(),
        incident.getStatus().name(),
        incident.getClosedAt(),
        incident.getClosedBy() != null ? incident.getClosedBy().getUsername() : null,
        incident.getCreatedAt(),
        incident.getUpdatedAt()
    );
  }
}
