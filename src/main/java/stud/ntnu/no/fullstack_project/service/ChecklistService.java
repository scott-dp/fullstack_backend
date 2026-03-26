package stud.ntnu.no.fullstack_project.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistAnswerResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistCompletionResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistItemResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistTemplateResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.CompleteChecklistRequest;
import stud.ntnu.no.fullstack_project.dto.checklist.CreateChecklistTemplateRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.ChecklistAnswer;
import stud.ntnu.no.fullstack_project.entity.ChecklistCompletion;
import stud.ntnu.no.fullstack_project.entity.ChecklistItem;
import stud.ntnu.no.fullstack_project.entity.ChecklistTemplate;
import stud.ntnu.no.fullstack_project.entity.CompletionStatus;
import stud.ntnu.no.fullstack_project.entity.ComplianceCategory;
import stud.ntnu.no.fullstack_project.entity.Frequency;
import stud.ntnu.no.fullstack_project.repository.ChecklistCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.ChecklistTemplateRepository;

/**
 * Service for managing checklist templates and their completions.
 *
 * <p>Provides business logic for creating, reading, updating, and soft-deleting
 * checklist templates, as well as recording and retrieving checklist completions.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistService {

  private final ChecklistTemplateRepository templateRepository;
  private final ChecklistCompletionRepository completionRepository;

  /**
   * Creates a new checklist template with its items.
   *
   * @param request     the template details including items
   * @param currentUser the authenticated user creating the template
   * @return the created template response
   */
  @Transactional
  public ChecklistTemplateResponse createTemplate(CreateChecklistTemplateRequest request,
      AppUser currentUser) {
    Frequency frequency;
    try {
      frequency = Frequency.valueOf(request.frequency());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid frequency: " + request.frequency());
    }

    ComplianceCategory category;
    try {
      category = ComplianceCategory.valueOf(request.category());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid category: " + request.category());
    }

    ChecklistTemplate template = new ChecklistTemplate();
    template.setTitle(request.title());
    template.setDescription(request.description());
    template.setFrequency(frequency);
    template.setCategory(category);
    template.setOrganization(currentUser.getOrganization());
    template.setCreatedBy(currentUser);
    template.setActive(true);

    request.items().forEach(itemReq -> {
      ChecklistItem item = new ChecklistItem();
      item.setTemplate(template);
      item.setDescription(itemReq.description());
      item.setSortOrder(itemReq.sortOrder());
      item.setRequiresComment(itemReq.requiresComment());
      template.getItems().add(item);
    });

    ChecklistTemplate saved = templateRepository.save(template);
    log.info("Checklist template created: {} (id={})", saved.getTitle(), saved.getId());
    return mapToTemplateResponse(saved);
  }

  /**
   * Retrieves a checklist template by its ID.
   *
   * @param id the template identifier
   * @return the template response
   */
  public ChecklistTemplateResponse getTemplate(Long id) {
    ChecklistTemplate template = templateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Checklist template not found with id: " + id));
    return mapToTemplateResponse(template);
  }

  /**
   * Lists active checklist templates for an organization, optionally filtered by category.
   *
   * @param organizationId the organization identifier
   * @param category       optional compliance category filter
   * @return list of matching template responses
   */
  public List<ChecklistTemplateResponse> listTemplates(Long organizationId, String category) {
    List<ChecklistTemplate> templates;

    if (category != null && !category.isBlank()) {
      ComplianceCategory cat;
      try {
        cat = ComplianceCategory.valueOf(category);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid category: " + category);
      }
      templates = templateRepository.findByOrganizationIdAndCategoryAndActiveTrue(
          organizationId, cat);
    } else {
      templates = templateRepository.findByOrganizationIdAndActiveTrue(organizationId);
    }

    return templates.stream()
        .map(this::mapToTemplateResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an existing checklist template with new details and items.
   *
   * @param id      the template identifier
   * @param request the updated template details
   * @return the updated template response
   */
  @Transactional
  public ChecklistTemplateResponse updateTemplate(Long id,
      CreateChecklistTemplateRequest request) {
    ChecklistTemplate template = templateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Checklist template not found with id: " + id));

    Frequency frequency;
    try {
      frequency = Frequency.valueOf(request.frequency());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid frequency: " + request.frequency());
    }

    ComplianceCategory category;
    try {
      category = ComplianceCategory.valueOf(request.category());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid category: " + request.category());
    }

    template.setTitle(request.title());
    template.setDescription(request.description());
    template.setFrequency(frequency);
    template.setCategory(category);

    template.getItems().clear();
    request.items().forEach(itemReq -> {
      ChecklistItem item = new ChecklistItem();
      item.setTemplate(template);
      item.setDescription(itemReq.description());
      item.setSortOrder(itemReq.sortOrder());
      item.setRequiresComment(itemReq.requiresComment());
      template.getItems().add(item);
    });

    ChecklistTemplate saved = templateRepository.save(template);
    log.info("Checklist template updated: {} (id={})", saved.getTitle(), saved.getId());
    return mapToTemplateResponse(saved);
  }

  /**
   * Soft-deletes a checklist template by marking it as inactive.
   *
   * @param id the template identifier
   */
  @Transactional
  public void deleteTemplate(Long id) {
    ChecklistTemplate template = templateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Checklist template not found with id: " + id));
    template.setActive(false);
    templateRepository.save(template);
    log.info("Checklist template soft-deleted: id={}", id);
  }

  /**
   * Completes a checklist by recording answers for each item.
   *
   * @param request     the completion request with answers
   * @param currentUser the authenticated user completing the checklist
   * @return the completion response
   */
  @Transactional
  public ChecklistCompletionResponse completeChecklist(CompleteChecklistRequest request,
      AppUser currentUser) {
    ChecklistTemplate template = templateRepository.findById(request.templateId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Checklist template not found with id: " + request.templateId()));

    Map<Long, ChecklistItem> itemMap = template.getItems().stream()
        .collect(Collectors.toMap(ChecklistItem::getId, Function.identity()));

    ChecklistCompletion completion = new ChecklistCompletion();
    completion.setTemplate(template);
    completion.setCompletedBy(currentUser);
    completion.setComment(request.comment());

    boolean allChecked = true;
    for (var answerReq : request.answers()) {
      ChecklistItem item = itemMap.get(answerReq.itemId());
      if (item == null) {
        throw new IllegalArgumentException(
            "Checklist item not found with id: " + answerReq.itemId());
      }

      ChecklistAnswer answer = new ChecklistAnswer();
      answer.setCompletion(completion);
      answer.setItem(item);
      answer.setChecked(answerReq.checked());
      answer.setComment(answerReq.comment());
      completion.getAnswers().add(answer);

      if (!answerReq.checked()) {
        allChecked = false;
      }
    }

    completion.setStatus(allChecked ? CompletionStatus.COMPLETE : CompletionStatus.INCOMPLETE);

    ChecklistCompletion saved = completionRepository.save(completion);
    log.info("Checklist completed: templateId={}, status={}", template.getId(), saved.getStatus());
    return mapToCompletionResponse(saved);
  }

  /**
   * Retrieves a checklist completion by its ID.
   *
   * @param id the completion identifier
   * @return the completion response
   */
  public ChecklistCompletionResponse getCompletion(Long id) {
    ChecklistCompletion completion = completionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Checklist completion not found with id: " + id));
    return mapToCompletionResponse(completion);
  }

  /**
   * Lists all checklist completions for an organization.
   *
   * @param organizationId the organization identifier
   * @return list of completion responses ordered by most recent first
   */
  public List<ChecklistCompletionResponse> listCompletions(Long organizationId) {
    return completionRepository
        .findByTemplateOrganizationIdOrderByCompletedAtDesc(organizationId).stream()
        .map(this::mapToCompletionResponse)
        .collect(Collectors.toList());
  }

  /**
   * Maps a checklist template entity to its response DTO.
   *
   * @param template the template entity
   * @return the template response DTO
   */
  private ChecklistTemplateResponse mapToTemplateResponse(ChecklistTemplate template) {
    List<ChecklistItemResponse> items = template.getItems().stream()
        .map(item -> new ChecklistItemResponse(
            item.getId(),
            item.getDescription(),
            item.getSortOrder(),
            item.isRequiresComment()
        ))
        .collect(Collectors.toList());

    return new ChecklistTemplateResponse(
        template.getId(),
        template.getTitle(),
        template.getDescription(),
        template.getFrequency().name(),
        template.getCategory().name(),
        template.isActive(),
        items,
        template.getCreatedBy() != null ? template.getCreatedBy().getUsername() : null,
        template.getCreatedAt()
    );
  }

  /**
   * Maps a checklist completion entity to its response DTO.
   *
   * @param completion the completion entity
   * @return the completion response DTO
   */
  private ChecklistCompletionResponse mapToCompletionResponse(ChecklistCompletion completion) {
    List<ChecklistAnswerResponse> answers = completion.getAnswers().stream()
        .map(answer -> new ChecklistAnswerResponse(
            answer.getId(),
            answer.getItem().getId(),
            answer.getItem().getDescription(),
            answer.isChecked(),
            answer.getComment()
        ))
        .collect(Collectors.toList());

    return new ChecklistCompletionResponse(
        completion.getId(),
        completion.getTemplate().getId(),
        completion.getTemplate().getTitle(),
        completion.getCompletedBy().getUsername(),
        completion.getCompletedAt(),
        completion.getStatus().name(),
        completion.getComment(),
        answers
    );
  }
}
