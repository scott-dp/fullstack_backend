package stud.ntnu.no.fullstack_project.service;

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
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.ComplianceCategory;
import stud.ntnu.no.fullstack_project.entity.Deviation;
import stud.ntnu.no.fullstack_project.entity.DeviationComment;
import stud.ntnu.no.fullstack_project.entity.DeviationSeverity;
import stud.ntnu.no.fullstack_project.entity.DeviationStatus;
import stud.ntnu.no.fullstack_project.entity.NotificationType;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.DeviationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviationService {

  private final DeviationRepository deviationRepository;
  private final AppUserRepository appUserRepository;
  private final NotificationService notificationService;

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

  public DeviationResponse getDeviation(Long id) {
    Deviation deviation = deviationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Deviation not found with id: " + id));
    return mapToResponse(deviation);
  }

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
    deviation.getComments().add(comment);

    deviationRepository.save(deviation);
    log.info("Comment added to deviation {}: by {}", deviationId, currentUser.getUsername());

    return mapToCommentResponse(comment);
  }

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

  private DeviationCommentResponse mapToCommentResponse(DeviationComment comment) {
    return new DeviationCommentResponse(
        comment.getId(),
        comment.getAuthor().getUsername(),
        comment.getContent(),
        comment.getCreatedAt()
    );
  }
}
