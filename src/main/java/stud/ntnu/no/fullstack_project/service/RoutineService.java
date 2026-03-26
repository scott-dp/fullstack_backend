package stud.ntnu.no.fullstack_project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.routine.CreateRoutineRequest;
import stud.ntnu.no.fullstack_project.dto.routine.ReviewRoutineRequest;
import stud.ntnu.no.fullstack_project.dto.routine.RoutineResponse;
import stud.ntnu.no.fullstack_project.dto.routine.RoutineReviewResponse;
import stud.ntnu.no.fullstack_project.dto.routine.UpdateRoutineRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.ChecklistTemplate;
import stud.ntnu.no.fullstack_project.entity.FrequencyType;
import stud.ntnu.no.fullstack_project.entity.ModuleType;
import stud.ntnu.no.fullstack_project.entity.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.Routine;
import stud.ntnu.no.fullstack_project.entity.RoutineCategory;
import stud.ntnu.no.fullstack_project.entity.RoutineReview;
import stud.ntnu.no.fullstack_project.repository.ChecklistTemplateRepository;
import stud.ntnu.no.fullstack_project.repository.RoutineRepository;
import stud.ntnu.no.fullstack_project.repository.RoutineReviewRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineService {

  private final RoutineRepository routineRepository;
  private final RoutineReviewRepository routineReviewRepository;
  private final ChecklistTemplateRepository checklistTemplateRepository;

  @Transactional
  public RoutineResponse createRoutine(CreateRoutineRequest request, AppUser currentUser) {
    ModuleType moduleType = parseEnum(ModuleType.class, request.moduleType(), "moduleType");
    RoutineCategory category = parseEnum(RoutineCategory.class, request.category(), "category");
    ResponsibleRole role = parseEnum(ResponsibleRole.class, request.responsibleRole(), "responsibleRole");
    FrequencyType freq = parseEnum(FrequencyType.class, request.frequencyType(), "frequencyType");

    Routine routine = new Routine();
    routine.setOrganization(currentUser.getOrganization());
    routine.setName(request.name());
    routine.setModuleType(moduleType);
    routine.setCategory(category);
    routine.setDescription(request.description());
    routine.setPurpose(request.purpose());
    routine.setResponsibleRole(role);
    routine.setFrequencyType(freq);
    routine.setStepsText(request.stepsText());
    routine.setWhatIsDeviationText(request.whatIsDeviationText());
    routine.setCorrectiveActionText(request.correctiveActionText());
    routine.setRequiredEvidenceText(request.requiredEvidenceText());
    routine.setReviewIntervalDays(request.reviewIntervalDays());
    routine.setCreatedBy(currentUser);

    if (request.linkedChecklistTemplateId() != null) {
      ChecklistTemplate template = checklistTemplateRepository
          .findById(request.linkedChecklistTemplateId())
          .orElseThrow(() -> new IllegalArgumentException(
              "Checklist template not found with id: " + request.linkedChecklistTemplateId()));
      routine.setLinkedChecklistTemplate(template);
    }

    Routine saved = routineRepository.save(routine);
    log.info("Routine created: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  public RoutineResponse getRoutine(Long id) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));
    return mapToResponse(routine);
  }

  public List<RoutineResponse> listRoutines(Long organizationId, String moduleType,
      String category, Boolean active) {
    List<Routine> routines;

    if (moduleType != null && !moduleType.isBlank()) {
      ModuleType mt = parseEnum(ModuleType.class, moduleType, "moduleType");
      routines = routineRepository.findByOrganizationIdAndModuleTypeOrderByCreatedAtDesc(
          organizationId, mt);
    } else if (category != null && !category.isBlank()) {
      RoutineCategory cat = parseEnum(RoutineCategory.class, category, "category");
      routines = routineRepository.findByOrganizationIdAndCategoryOrderByCreatedAtDesc(
          organizationId, cat);
    } else if (Boolean.TRUE.equals(active)) {
      routines = routineRepository.findByOrganizationIdAndActiveTrueOrderByCreatedAtDesc(
          organizationId);
    } else {
      routines = routineRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    return routines.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public RoutineResponse updateRoutine(Long id, UpdateRoutineRequest request,
      AppUser currentUser) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));

    boolean changed = false;

    if (request.name() != null && !request.name().isBlank()) {
      routine.setName(request.name());
      changed = true;
    }
    if (request.moduleType() != null) {
      routine.setModuleType(parseEnum(ModuleType.class, request.moduleType(), "moduleType"));
      changed = true;
    }
    if (request.category() != null) {
      routine.setCategory(parseEnum(RoutineCategory.class, request.category(), "category"));
      changed = true;
    }
    if (request.description() != null) {
      routine.setDescription(request.description());
      changed = true;
    }
    if (request.purpose() != null) {
      routine.setPurpose(request.purpose());
      changed = true;
    }
    if (request.responsibleRole() != null) {
      routine.setResponsibleRole(
          parseEnum(ResponsibleRole.class, request.responsibleRole(), "responsibleRole"));
      changed = true;
    }
    if (request.frequencyType() != null) {
      routine.setFrequencyType(
          parseEnum(FrequencyType.class, request.frequencyType(), "frequencyType"));
      changed = true;
    }
    if (request.stepsText() != null) {
      routine.setStepsText(request.stepsText());
      changed = true;
    }
    if (request.whatIsDeviationText() != null) {
      routine.setWhatIsDeviationText(request.whatIsDeviationText());
      changed = true;
    }
    if (request.correctiveActionText() != null) {
      routine.setCorrectiveActionText(request.correctiveActionText());
      changed = true;
    }
    if (request.requiredEvidenceText() != null) {
      routine.setRequiredEvidenceText(request.requiredEvidenceText());
      changed = true;
    }
    if (request.reviewIntervalDays() != null) {
      routine.setReviewIntervalDays(request.reviewIntervalDays());
      changed = true;
    }
    if (request.linkedChecklistTemplateId() != null) {
      if (request.linkedChecklistTemplateId() == 0) {
        routine.setLinkedChecklistTemplate(null);
      } else {
        ChecklistTemplate template = checklistTemplateRepository
            .findById(request.linkedChecklistTemplateId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Checklist template not found with id: " + request.linkedChecklistTemplateId()));
        routine.setLinkedChecklistTemplate(template);
      }
      changed = true;
    }

    if (changed) {
      routine.setVersionNumber(routine.getVersionNumber() + 1);
    }

    Routine saved = routineRepository.save(routine);
    log.info("Routine updated: id={}, version={}", saved.getId(), saved.getVersionNumber());
    return mapToResponse(saved);
  }

  @Transactional
  public RoutineResponse archiveRoutine(Long id) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));
    routine.setActive(false);
    Routine saved = routineRepository.save(routine);
    log.info("Routine archived: id={}", saved.getId());
    return mapToResponse(saved);
  }

  @Transactional
  public RoutineResponse unarchiveRoutine(Long id) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));
    routine.setActive(true);
    Routine saved = routineRepository.save(routine);
    log.info("Routine unarchived: id={}", saved.getId());
    return mapToResponse(saved);
  }

  @Transactional
  public void deleteRoutine(Long id) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));
    routineReviewRepository.deleteByRoutineId(id);
    routineRepository.delete(routine);
    log.info("Routine deleted: id={}", id);
  }

  @Transactional
  public RoutineReviewResponse reviewRoutine(Long id, ReviewRoutineRequest request,
      AppUser currentUser) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));

    LocalDateTime now = LocalDateTime.now();
    routine.setLastReviewedAt(now);
    routineRepository.save(routine);

    RoutineReview review = new RoutineReview();
    review.setRoutine(routine);
    review.setReviewedBy(currentUser);
    review.setReviewedAt(now);
    review.setNotes(request.notes());

    if (routine.getReviewIntervalDays() != null) {
      review.setNextReviewAt(now.plusDays(routine.getReviewIntervalDays()));
    }

    RoutineReview saved = routineReviewRepository.save(review);
    log.info("Routine reviewed: routineId={}, reviewId={}", id, saved.getId());
    return mapToReviewResponse(saved);
  }

  public List<RoutineReviewResponse> getRoutineHistory(Long routineId) {
    routineRepository.findById(routineId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Routine not found with id: " + routineId));

    return routineReviewRepository.findByRoutineIdOrderByReviewedAtDesc(routineId)
        .stream()
        .map(this::mapToReviewResponse)
        .collect(Collectors.toList());
  }

  private RoutineResponse mapToResponse(Routine routine) {
    return new RoutineResponse(
        routine.getId(),
        routine.getName(),
        routine.getModuleType().name(),
        routine.getCategory().name(),
        routine.getDescription(),
        routine.getPurpose(),
        routine.getResponsibleRole().name(),
        routine.getFrequencyType().name(),
        routine.getStepsText(),
        routine.getWhatIsDeviationText(),
        routine.getCorrectiveActionText(),
        routine.getRequiredEvidenceText(),
        routine.getLinkedChecklistTemplate() != null
            ? routine.getLinkedChecklistTemplate().getId() : null,
        routine.getLinkedChecklistTemplate() != null
            ? routine.getLinkedChecklistTemplate().getTitle() : null,
        routine.isActive(),
        routine.getReviewIntervalDays(),
        routine.getLastReviewedAt(),
        routine.getVersionNumber(),
        routine.getCreatedBy() != null ? routine.getCreatedBy().getUsername() : null,
        routine.getCreatedAt(),
        routine.getUpdatedAt()
    );
  }

  private RoutineReviewResponse mapToReviewResponse(RoutineReview review) {
    return new RoutineReviewResponse(
        review.getId(),
        review.getRoutine().getId(),
        review.getReviewedBy().getUsername(),
        review.getReviewedAt(),
        review.getNotes(),
        review.getNextReviewAt()
    );
  }

  private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
    try {
      return Enum.valueOf(enumClass, value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
    }
  }
}
