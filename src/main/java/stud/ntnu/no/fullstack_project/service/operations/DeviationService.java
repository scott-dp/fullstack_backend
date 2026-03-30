package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.deviation.AddDeviationCommentRequest;
import stud.ntnu.no.fullstack_project.dto.deviation.CreateDeviationRequest;
import stud.ntnu.no.fullstack_project.dto.deviation.DeviationCommentResponse;
import stud.ntnu.no.fullstack_project.dto.deviation.DeviationResponse;
import stud.ntnu.no.fullstack_project.dto.deviation.UpdateDeviationRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.ComplianceCategory;
import stud.ntnu.no.fullstack_project.entity.operations.Deviation;
import stud.ntnu.no.fullstack_project.entity.operations.DeviationComment;
import stud.ntnu.no.fullstack_project.entity.operations.DeviationSeverity;
import stud.ntnu.no.fullstack_project.entity.operations.DeviationStatus;
import stud.ntnu.no.fullstack_project.entity.notifications.NotificationType;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.operations.DeviationRepository;

/**
 * Service for managing compliance deviations.
 *
 * <p>Handles creation, retrieval, filtering, status updates, assignment, and
 * commenting on deviations within an organization.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviationService {

  private final DeviationRepository deviationRepository;
  private final AppUserRepository appUserRepository;
  private final NotificationService notificationService;

  /**
   * Creates a new deviation and persists it.
   *
   * @param request     the deviation details
   * @param currentUser the authenticated user reporting the deviation
   * @return the created deviation response
   */
  @Transactional
  public DeviationResponse createDeviation(CreateDeviationRequest request,
      AppUser currentUser) {
    ComplianceCategory category;
    try {
      category = ComplianceCategory.valueOf(request.category());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid category: " + request.category());
    }

    DeviationSeverity severity;
    try {
      severity = DeviationSeverity.valueOf(request.severity());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid severity: " + request.severity());
    }

    Deviation deviation = new Deviation();
    deviation.setOrganization(currentUser.getOrganization());
    deviation.setTitle(request.title());
    deviation.setDescription(request.description());
    deviation.setCategory(category);
    deviation.setSeverity(severity);
    deviation.setStatus(DeviationStatus.OPEN);
    deviation.setReportedBy(currentUser);

    Deviation saved = deviationRepository.save(deviation);
    log.info("Deviation created: {} (id={})", saved.getTitle(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a deviation by its ID, including its comments.
   *
   * @param id the deviation identifier
   * @return the deviation response with comments
   */
  public DeviationResponse getDeviation(Long id) {
    Deviation deviation = deviationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Deviation not found with id: " + id));
    return mapToResponse(deviation);
  }

  /**
   * Lists deviations for an organization, optionally filtered by status or category.
   *
   * @param organizationId the organization identifier
   * @param status         optional status filter (e.g. OPEN, IN_PROGRESS)
   * @param category       optional compliance category filter
   * @return list of matching deviation responses
   */
  public List<DeviationResponse> listDeviations(Long organizationId, String status,
      String category) {
    List<Deviation> deviations;

    if (status != null && !status.isBlank()) {
      DeviationStatus deviationStatus;
      try {
        deviationStatus = DeviationStatus.valueOf(status);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + status);
      }
      deviations = deviationRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(
          organizationId, deviationStatus);
    } else if (category != null && !category.isBlank()) {
      ComplianceCategory cat;
      try {
        cat = ComplianceCategory.valueOf(category);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid category: " + category);
      }
      deviations = deviationRepository.findByOrganizationIdAndCategoryOrderByCreatedAtDesc(
          organizationId, cat);
    } else {
      deviations = deviationRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    return deviations.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates a deviation's status and/or assignment.
   *
   * <p>If the status is changed to RESOLVED, the resolver and resolution timestamp
   * are recorded. If an assignee is specified, a notification is sent to them.</p>
   *
   * @param id          the deviation identifier
   * @param request     the fields to update
   * @param currentUser the authenticated user performing the update
   * @return the updated deviation response
   */
  @Transactional
  public DeviationResponse updateDeviation(Long id, UpdateDeviationRequest request,
      AppUser currentUser) {
    Deviation deviation = deviationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Deviation not found with id: " + id));

    if (request.status() != null && !request.status().isBlank()) {
      DeviationStatus newStatus;
      try {
        newStatus = DeviationStatus.valueOf(request.status());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + request.status());
      }

      deviation.setStatus(newStatus);

      if (newStatus == DeviationStatus.RESOLVED) {
        deviation.setResolvedBy(currentUser);
        deviation.setResolvedAt(LocalDateTime.now());
      }
    }

    if (request.assignedToId() != null) {
      AppUser assignee = appUserRepository.findById(request.assignedToId())
          .orElseThrow(() -> new IllegalArgumentException(
              "User not found with id: " + request.assignedToId()));
      deviation.setAssignedTo(assignee);

      notificationService.createNotification(
          assignee,
          "Deviation Assigned",
          String.format("You have been assigned to deviation: %s", deviation.getTitle()),
          NotificationType.DEVIATION_ASSIGNED,
          deviation.getId(),
          "DEVIATION"
      );
    }

    Deviation saved = deviationRepository.save(deviation);
    log.info("Deviation updated: id={}, status={}", saved.getId(), saved.getStatus());
    return mapToResponse(saved);
  }

  /**
   * Adds a comment to an existing deviation.
   *
   * @param deviationId the deviation identifier
   * @param request     the comment content
   * @param currentUser the authenticated user authoring the comment
   * @return the created comment response
   */
  @Transactional
  public DeviationCommentResponse addComment(Long deviationId,
      AddDeviationCommentRequest request, AppUser currentUser) {
    Deviation deviation = deviationRepository.findById(deviationId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Deviation not found with id: " + deviationId));

    DeviationComment comment = new DeviationComment();
    comment.setDeviation(deviation);
    comment.setAuthor(currentUser);
    comment.setContent(request.content());
    comment.setCreatedAt(LocalDateTime.now());
    deviation.getComments().add(comment);

    deviationRepository.save(deviation);
    log.info("Comment added to deviation {}: by {}", deviationId, currentUser.getUsername());

    return mapToCommentResponse(comment);
  }

  /**
   * Maps a deviation entity to its response DTO.
   *
   * @param deviation the deviation entity
   * @return the deviation response DTO
   */
  private DeviationResponse mapToResponse(Deviation deviation) {
    List<DeviationCommentResponse> comments = deviation.getComments().stream()
        .map(this::mapToCommentResponse)
        .collect(Collectors.toList());

    return new DeviationResponse(
        deviation.getId(),
        deviation.getTitle(),
        deviation.getDescription(),
        deviation.getCategory().name(),
        deviation.getSeverity().name(),
        deviation.getStatus().name(),
        deviation.getReportedBy().getUsername(),
        deviation.getAssignedTo() != null ? deviation.getAssignedTo().getUsername() : null,
        deviation.getResolvedBy() != null ? deviation.getResolvedBy().getUsername() : null,
        deviation.getResolvedAt(),
        deviation.getCreatedAt(),
        deviation.getUpdatedAt(),
        comments
    );
  }

  /**
   * Maps a deviation comment entity to its response DTO.
   *
   * @param comment the comment entity
   * @return the comment response DTO
   */
  private DeviationCommentResponse mapToCommentResponse(DeviationComment comment) {
    return new DeviationCommentResponse(
        comment.getId(),
        comment.getAuthor().getUsername(),
        comment.getContent(),
        comment.getCreatedAt()
    );
  }
}
