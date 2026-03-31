package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.training.AssignTrainingRequest;
import stud.ntnu.no.fullstack_project.dto.training.CompleteTrainingRequest;
import stud.ntnu.no.fullstack_project.dto.training.CreateTrainingTemplateRequest;
import stud.ntnu.no.fullstack_project.dto.training.TrainingAssignmentResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingCompletionResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingReportResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingTemplateResponse;
import stud.ntnu.no.fullstack_project.dto.training.UpdateTrainingTemplateRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.ModuleType;
import stud.ntnu.no.fullstack_project.entity.notifications.NotificationType;
import stud.ntnu.no.fullstack_project.entity.auth.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.training.TrainingAssignment;
import stud.ntnu.no.fullstack_project.entity.training.TrainingAssignmentStatus;
import stud.ntnu.no.fullstack_project.entity.training.TrainingCategory;
import stud.ntnu.no.fullstack_project.entity.training.TrainingCompletion;
import stud.ntnu.no.fullstack_project.entity.training.TrainingTemplate;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.training.TrainingAssignmentRepository;
import stud.ntnu.no.fullstack_project.repository.training.TrainingCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.training.TrainingTemplateRepository;

/**
 * Service for managing training templates, assignments, and completions.
 *
 * <p>Handles creation, retrieval, assignment to users, completion tracking,
 * and reporting of training activities within an organization.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

  private final TrainingTemplateRepository trainingTemplateRepository;
  private final TrainingAssignmentRepository trainingAssignmentRepository;
  private final TrainingCompletionRepository trainingCompletionRepository;
  private final AppUserRepository appUserRepository;
  private final NotificationService notificationService;

  /**
   * Creates a new training template and persists it.
   *
   * @param request     the template details
   * @param currentUser the authenticated user creating the template
   * @return the created template response
   */
  @Transactional
  public TrainingTemplateResponse createTemplate(CreateTrainingTemplateRequest request,
      AppUser currentUser) {
    ModuleType moduleType;
    try {
      moduleType = ModuleType.valueOf(request.moduleType());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid module type: " + request.moduleType());
    }

    TrainingCategory category;
    try {
      category = TrainingCategory.valueOf(request.category());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid category: " + request.category());
    }

    ResponsibleRole requiredForRole;
    try {
      requiredForRole = ResponsibleRole.valueOf(request.requiredForRole());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid required role: " + request.requiredForRole());
    }

    TrainingTemplate template = new TrainingTemplate();
    template.setOrganization(currentUser.getOrganization());
    template.setTitle(request.title());
    template.setModuleType(moduleType);
    template.setDescription(request.description());
    template.setContentText(request.contentText());
    template.setCategory(category);
    template.setRequiredForRole(requiredForRole);
    template.setMandatory(request.isMandatory());
    template.setValidityDays(request.validityDays());
    template.setAcknowledgmentRequired(request.acknowledgmentRequired());

    TrainingTemplate saved = trainingTemplateRepository.save(template);
    log.info("Training template created: {} (id={})", saved.getTitle(), saved.getId());
    return mapToTemplateResponse(saved);
  }

  /**
   * Retrieves a training template by its ID.
   *
   * @param id the template identifier
   * @return the template response
   */
  public TrainingTemplateResponse getTemplate(Long id) {
    TrainingTemplate template = trainingTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Training template not found with id: " + id));
    return mapToTemplateResponse(template);
  }

  /**
   * Lists all training templates for an organization.
   *
   * @param organizationId the organization identifier
   * @return list of template responses
   */
  public List<TrainingTemplateResponse> listTemplates(Long organizationId) {
    return trainingTemplateRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
        .stream()
        .map(this::mapToTemplateResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an existing training template.
   *
   * @param id      the template identifier
   * @param request the fields to update
   * @return the updated template response
   */
  @Transactional
  public TrainingTemplateResponse updateTemplate(Long id,
      UpdateTrainingTemplateRequest request) {
    TrainingTemplate template = trainingTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Training template not found with id: " + id));

    if (request.title() != null && !request.title().isBlank()) {
      template.setTitle(request.title());
    }
    if (request.moduleType() != null && !request.moduleType().isBlank()) {
      ModuleType moduleType;
      try {
        moduleType = ModuleType.valueOf(request.moduleType());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid module type: " + request.moduleType());
      }
      template.setModuleType(moduleType);
    }
    if (request.category() != null && !request.category().isBlank()) {
      TrainingCategory category;
      try {
        category = TrainingCategory.valueOf(request.category());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid category: " + request.category());
      }
      template.setCategory(category);
    }
    if (request.description() != null) {
      template.setDescription(request.description());
    }
    if (request.contentText() != null) {
      template.setContentText(request.contentText());
    }
    if (request.requiredForRole() != null && !request.requiredForRole().isBlank()) {
      ResponsibleRole role;
      try {
        role = ResponsibleRole.valueOf(request.requiredForRole());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Invalid required role: " + request.requiredForRole());
      }
      template.setRequiredForRole(role);
    }
    if (request.isMandatory() != null) {
      template.setMandatory(request.isMandatory());
    }
    if (request.validityDays() != null) {
      template.setValidityDays(request.validityDays());
    }
    if (request.acknowledgmentRequired() != null) {
      template.setAcknowledgmentRequired(request.acknowledgmentRequired());
    }
    if (request.active() != null) {
      template.setActive(request.active());
    }

    TrainingTemplate saved = trainingTemplateRepository.save(template);
    log.info("Training template updated: id={}", saved.getId());
    return mapToTemplateResponse(saved);
  }

  /**
   * Assigns a training template to one or more users.
   *
   * @param templateId  the template identifier
   * @param request     the assignment details
   * @param currentUser the authenticated user performing the assignment
   * @return list of created assignment responses
   */
  @Transactional
  public List<TrainingAssignmentResponse> assignTraining(Long templateId,
      AssignTrainingRequest request, AppUser currentUser) {
    TrainingTemplate template = trainingTemplateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Training template not found with id: " + templateId));

    LocalDateTime dueAt = null;
    if (request.dueAt() != null && !request.dueAt().isBlank()) {
      dueAt = parseDueAt(request.dueAt());
    }

    List<TrainingAssignmentResponse> responses = new ArrayList<>();

    for (Long userId : request.assigneeUserIds()) {
      AppUser assignee = appUserRepository.findById(userId)
          .orElseThrow(() -> new IllegalArgumentException(
              "User not found with id: " + userId));

      TrainingAssignment assignment = new TrainingAssignment();
      assignment.setTrainingTemplate(template);
      assignment.setOrganization(currentUser.getOrganization());
      assignment.setAssigneeUser(assignee);
      assignment.setAssignedBy(currentUser);
      assignment.setDueAt(dueAt);

      TrainingAssignment saved = trainingAssignmentRepository.save(assignment);
      notificationService.createNotification(
          assignee,
          "Training Assigned",
          String.format("You have been assigned training: %s", template.getTitle()),
          NotificationType.TRAINING_ASSIGNED,
          saved.getId(),
          "TRAINING_ASSIGNMENT"
      );
      log.info("Training assigned: templateId={}, userId={}, assignmentId={}",
          templateId, userId, saved.getId());
      responses.add(mapToAssignmentResponse(saved));
    }

    return responses;
  }

  /**
   * Retrieves all assignments for the current user.
   *
   * @param currentUser the authenticated user
   * @return list of assignment responses
   */
  public List<TrainingAssignmentResponse> getMyAssignments(AppUser currentUser) {
    return trainingAssignmentRepository
        .findByAssigneeUserIdOrderByAssignedAtDesc(currentUser.getId())
        .stream()
        .map(this::mapToAssignmentResponse)
        .collect(Collectors.toList());
  }

  /**
   * Completes a training assignment.
   *
   * <p>If the template requires acknowledgment and it is not checked, an error is thrown.
   * If the template has validity days, the expiry date is calculated from the completion time.</p>
   *
   * @param assignmentId the assignment identifier
   * @param request      the completion details
   * @param currentUser  the authenticated user completing the assignment
   * @return the completion response
   */
  @Transactional
  public TrainingCompletionResponse completeAssignment(Long assignmentId,
      CompleteTrainingRequest request, AppUser currentUser) {
    TrainingAssignment assignment = trainingAssignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Training assignment not found with id: " + assignmentId));

    TrainingTemplate template = assignment.getTrainingTemplate();

    if (template.isAcknowledgmentRequired() && !request.acknowledgementChecked()) {
      throw new IllegalArgumentException(
          "Acknowledgment is required to complete this training.");
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = null;
    if (template.getValidityDays() != null) {
      expiresAt = now.plusDays(template.getValidityDays());
    }

    TrainingCompletion completion = new TrainingCompletion();
    completion.setTrainingAssignment(assignment);
    completion.setCompletedByUser(currentUser);
    completion.setAcknowledgementChecked(request.acknowledgementChecked());
    completion.setComments(request.comments());
    completion.setExpiresAt(expiresAt);

    TrainingCompletion saved = trainingCompletionRepository.save(completion);

    assignment.setStatus(TrainingAssignmentStatus.COMPLETED);
    trainingAssignmentRepository.save(assignment);

    log.info("Training assignment completed: assignmentId={}, userId={}",
        assignmentId, currentUser.getId());
    return mapToCompletionResponse(saved);
  }

  /**
   * Generates a training report for the current user's organization.
   *
   * @param organizationId the organization identifier
   * @return the training report response
   */
  public TrainingReportResponse getReport(Long organizationId) {
    long totalTemplates = trainingTemplateRepository
        .findByOrganizationIdOrderByCreatedAtDesc(organizationId).size();

    List<TrainingAssignment> allAssignments = trainingAssignmentRepository
        .findByTrainingTemplateOrganizationIdOrderByAssignedAtDesc(organizationId);
    long totalAssignments = allAssignments.size();

    long completedCount = trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(
            organizationId, TrainingAssignmentStatus.COMPLETED);

    long overdueCount = trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(
            organizationId, TrainingAssignmentStatus.OVERDUE);

    double completionRate = totalAssignments > 0
        ? (double) completedCount / totalAssignments * 100.0
        : 0.0;

    return new TrainingReportResponse(
        totalTemplates, totalAssignments, completedCount, overdueCount, completionRate);
  }

  /**
   * Deletes a training template together with its assignments, completions,
   * and assignment notifications.
   *
   * @param id training template identifier
   */
  @Transactional
  public void deleteTemplate(Long id) {
    TrainingTemplate template = trainingTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Training template not found with id: " + id));

    List<TrainingAssignment> assignments = trainingAssignmentRepository.findByTrainingTemplateId(id);
    List<Long> assignmentIds = assignments.stream()
        .map(TrainingAssignment::getId)
        .toList();

    if (!assignmentIds.isEmpty()) {
      trainingCompletionRepository.deleteByTrainingAssignmentIdIn(assignmentIds);
      notificationService.deleteNotificationsForReferences("TRAINING_ASSIGNMENT", assignmentIds);
      trainingAssignmentRepository.deleteAll(assignments);
    }

    trainingTemplateRepository.delete(template);
    log.info("Training template deleted: id={}", id);
  }

  /**
   * Maps a training template entity to its response DTO.
   *
   * @param template the template entity
   * @return the template response DTO
   */
  private TrainingTemplateResponse mapToTemplateResponse(TrainingTemplate template) {
    return new TrainingTemplateResponse(
        template.getId(),
        template.getOrganization().getId(),
        template.getTitle(),
        template.getModuleType().name(),
        template.getDescription(),
        template.getContentText(),
        template.getCategory().name(),
        template.getRequiredForRole().name(),
        template.isMandatory(),
        template.getValidityDays(),
        template.isAcknowledgmentRequired(),
        template.isActive(),
        template.getCreatedAt(),
        template.getUpdatedAt()
    );
  }

  /**
   * Maps a training assignment entity to its response DTO.
   *
   * @param assignment the assignment entity
   * @return the assignment response DTO
   */
  private TrainingAssignmentResponse mapToAssignmentResponse(TrainingAssignment assignment) {
    return new TrainingAssignmentResponse(
        assignment.getId(),
        assignment.getTrainingTemplate().getId(),
        assignment.getTrainingTemplate().getTitle(),
        assignment.getAssigneeUser().getUsername(),
        assignment.getAssignedBy().getUsername(),
        assignment.getAssignedAt(),
        assignment.getDueAt(),
        assignment.getStatus().name()
    );
  }

  /**
   * Maps a training completion entity to its response DTO.
   *
   * @param completion the completion entity
   * @return the completion response DTO
   */
  private TrainingCompletionResponse mapToCompletionResponse(TrainingCompletion completion) {
    return new TrainingCompletionResponse(
        completion.getId(),
        completion.getTrainingAssignment().getId(),
        completion.getCompletedByUser().getUsername(),
        completion.getCompletedAt(),
        completion.isAcknowledgementChecked(),
        completion.getComments(),
        completion.getExpiresAt()
    );
  }

  /**
   * Parses an assignment due date from either a full ISO date-time string or a
   * date-only string.
   *
   * <p>Date-only input is normalized to the start of that day.</p>
   *
   * @param dueAt raw due date input from the request
   * @return parsed due timestamp
   */
  private LocalDateTime parseDueAt(String dueAt) {
    try {
      return LocalDateTime.parse(dueAt);
    } catch (RuntimeException ignored) {
      return LocalDate.parse(dueAt).atStartOfDay();
    }
  }
}
