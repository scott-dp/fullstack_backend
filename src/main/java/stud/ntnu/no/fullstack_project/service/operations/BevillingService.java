package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.bevilling.BevillingResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.ConditionResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.CreateBevillingRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.CreateConditionRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.ServingHoursEntry;
import stud.ntnu.no.fullstack_project.dto.bevilling.ServingHoursResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.UpdateBevillingRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.UpdateConditionRequest;
import stud.ntnu.no.fullstack_project.entity.licensing.AlcoholGroup;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.licensing.Bevilling;
import stud.ntnu.no.fullstack_project.entity.licensing.BevillingCondition;
import stud.ntnu.no.fullstack_project.entity.licensing.BevillingServingHours;
import stud.ntnu.no.fullstack_project.entity.licensing.BevillingStatus;
import stud.ntnu.no.fullstack_project.entity.licensing.BevillingType;
import stud.ntnu.no.fullstack_project.entity.operations.ConditionType;
import stud.ntnu.no.fullstack_project.entity.operations.Weekday;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingConditionRepository;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingRepository;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingServingHoursRepository;

/**
 * Service for managing bevilling (alcohol license) records, conditions, and serving hours.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BevillingService {

  private final BevillingRepository bevillingRepository;
  private final BevillingConditionRepository conditionRepository;
  private final BevillingServingHoursRepository servingHoursRepository;

  /**
   * Creates a new bevilling for the current user's organization.
   *
   * @param request     the bevilling details
   * @param currentUser the authenticated user
   * @return the created bevilling response
   */
  @Transactional
  public BevillingResponse create(CreateBevillingRequest request, AppUser currentUser) {
    BevillingType type;
    try {
      type = BevillingType.valueOf(request.bevillingType());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid bevilling type: " + request.bevillingType());
    }

    Bevilling bevilling = new Bevilling();
    bevilling.setOrganization(currentUser.getOrganization());
    bevilling.setMunicipality(request.municipality());
    bevilling.setBevillingType(type);
    bevilling.setValidFrom(LocalDate.parse(request.validFrom()));
    bevilling.setValidTo(request.validTo() != null ? LocalDate.parse(request.validTo()) : null);
    bevilling.setLicenseNumber(request.licenseNumber());
    bevilling.setStatus(BevillingStatus.ACTIVE);

    if (request.alcoholGroupsAllowed() != null) {
      bevilling.setAlcoholGroupsAllowed(parseAlcoholGroups(request.alcoholGroupsAllowed()));
    }

    if (request.servingAreaDescription() != null) {
      bevilling.setServingAreaDescription(request.servingAreaDescription());
    }
    if (request.indoorAllowed() != null) {
      bevilling.setIndoorAllowed(request.indoorAllowed());
    }
    if (request.outdoorAllowed() != null) {
      bevilling.setOutdoorAllowed(request.outdoorAllowed());
    }
    bevilling.setStyrerName(request.styrerName());
    bevilling.setStedfortrederName(request.stedfortrederName());
    bevilling.setNotes(request.notes());

    Bevilling saved = bevillingRepository.save(bevilling);
    log.info("Bevilling created: id={}, type={}", saved.getId(), saved.getBevillingType());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a bevilling by its ID.
   *
   * @param id the bevilling identifier
   * @return the bevilling response
   */
  public BevillingResponse get(Long id) {
    Bevilling bevilling = bevillingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Bevilling not found with id: " + id));
    return mapToResponse(bevilling);
  }

  /**
   * Lists all bevillinger for an organization.
   *
   * @param organizationId the organization identifier
   * @return list of bevilling responses
   */
  public List<BevillingResponse> list(Long organizationId) {
    return bevillingRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Gets the current active bevilling for an organization.
   *
   * @param organizationId the organization identifier
   * @return the active bevilling response, or null if none
   */
  public BevillingResponse getCurrent(Long organizationId) {
    return bevillingRepository.findByOrganizationIdAndStatus(organizationId, BevillingStatus.ACTIVE)
        .map(this::mapToResponse)
        .orElse(null);
  }

  /**
   * Updates an existing bevilling.
   *
   * @param id      the bevilling identifier
   * @param request the fields to update
   * @return the updated bevilling response
   */
  @Transactional
  public BevillingResponse update(Long id, UpdateBevillingRequest request) {
    Bevilling bevilling = bevillingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Bevilling not found with id: " + id));

    if (request.municipality() != null && !request.municipality().isBlank()) {
      bevilling.setMunicipality(request.municipality());
    }
    if (request.bevillingType() != null && !request.bevillingType().isBlank()) {
      try {
        bevilling.setBevillingType(BevillingType.valueOf(request.bevillingType()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid bevilling type: " + request.bevillingType());
      }
    }
    if (request.validFrom() != null && !request.validFrom().isBlank()) {
      bevilling.setValidFrom(LocalDate.parse(request.validFrom()));
    }
    if (request.validTo() != null) {
      bevilling.setValidTo(request.validTo().isBlank() ? null : LocalDate.parse(request.validTo()));
    }
    if (request.licenseNumber() != null) {
      bevilling.setLicenseNumber(request.licenseNumber());
    }
    if (request.status() != null && !request.status().isBlank()) {
      try {
        bevilling.setStatus(BevillingStatus.valueOf(request.status()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + request.status());
      }
    }
    if (request.alcoholGroupsAllowed() != null) {
      bevilling.setAlcoholGroupsAllowed(parseAlcoholGroups(request.alcoholGroupsAllowed()));
    }
    if (request.servingAreaDescription() != null) {
      bevilling.setServingAreaDescription(request.servingAreaDescription());
    }
    if (request.indoorAllowed() != null) {
      bevilling.setIndoorAllowed(request.indoorAllowed());
    }
    if (request.outdoorAllowed() != null) {
      bevilling.setOutdoorAllowed(request.outdoorAllowed());
    }
    if (request.styrerName() != null) {
      bevilling.setStyrerName(request.styrerName());
    }
    if (request.stedfortrederName() != null) {
      bevilling.setStedfortrederName(request.stedfortrederName());
    }
    if (request.notes() != null) {
      bevilling.setNotes(request.notes());
    }

    Bevilling saved = bevillingRepository.save(bevilling);
    log.info("Bevilling updated: id={}, status={}", saved.getId(), saved.getStatus());
    return mapToResponse(saved);
  }

  /**
   * Adds a condition to a bevilling.
   *
   * @param bevillingId the bevilling identifier
   * @param request     the condition details
   * @return the created condition response
   */
  @Transactional
  public ConditionResponse addCondition(Long bevillingId, CreateConditionRequest request) {
    Bevilling bevilling = bevillingRepository.findById(bevillingId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Bevilling not found with id: " + bevillingId));

    ConditionType conditionType;
    try {
      conditionType = ConditionType.valueOf(request.conditionType());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid condition type: " + request.conditionType());
    }

    BevillingCondition condition = new BevillingCondition();
    condition.setBevilling(bevilling);
    condition.setConditionType(conditionType);
    condition.setTitle(request.title());
    condition.setDescription(request.description());
    condition.setActive(true);

    BevillingCondition saved = conditionRepository.save(condition);
    log.info("Condition added to bevilling {}: id={}", bevillingId, saved.getId());
    return mapToConditionResponse(saved);
  }

  /**
   * Updates a bevilling condition.
   *
   * @param conditionId the condition identifier
   * @param request     the fields to update
   * @return the updated condition response
   */
  @Transactional
  public ConditionResponse updateCondition(Long conditionId, UpdateConditionRequest request) {
    BevillingCondition condition = conditionRepository.findById(conditionId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Condition not found with id: " + conditionId));

    if (request.title() != null && !request.title().isBlank()) {
      condition.setTitle(request.title());
    }
    if (request.description() != null) {
      condition.setDescription(request.description());
    }
    if (request.active() != null) {
      condition.setActive(request.active());
    }

    BevillingCondition saved = conditionRepository.save(condition);
    log.info("Condition updated: id={}", saved.getId());
    return mapToConditionResponse(saved);
  }

  /**
   * Replaces all serving hours for a bevilling.
   *
   * @param bevillingId the bevilling identifier
   * @param entries     the serving hours entries
   * @return list of saved serving hours responses
   */
  @Transactional
  public List<ServingHoursResponse> setServingHours(Long bevillingId,
      List<ServingHoursEntry> entries) {
    Bevilling bevilling = bevillingRepository.findById(bevillingId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Bevilling not found with id: " + bevillingId));

    servingHoursRepository.deleteByBevillingId(bevillingId);

    List<BevillingServingHours> hoursList = entries.stream().map(entry -> {
      Weekday weekday;
      try {
        weekday = Weekday.valueOf(entry.weekday());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid weekday: " + entry.weekday());
      }

      BevillingServingHours hours = new BevillingServingHours();
      hours.setBevilling(bevilling);
      hours.setWeekday(weekday);
      hours.setStartTime(LocalTime.parse(entry.startTime()));
      hours.setEndTime(LocalTime.parse(entry.endTime()));
      hours.setConsumptionDeadlineMinutesAfterEnd(
          entry.consumptionDeadlineMinutesAfterEnd() != null
              ? entry.consumptionDeadlineMinutesAfterEnd() : 30);
      return hours;
    }).collect(Collectors.toList());

    List<BevillingServingHours> saved = servingHoursRepository.saveAll(hoursList);
    log.info("Serving hours set for bevilling {}: {} entries", bevillingId, saved.size());
    return saved.stream().map(this::mapToServingHoursResponse).collect(Collectors.toList());
  }

  /**
   * Gets serving hours for a bevilling.
   *
   * @param bevillingId the bevilling identifier
   * @return list of serving hours responses
   */
  public List<ServingHoursResponse> getServingHours(Long bevillingId) {
    return servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(bevillingId)
        .stream()
        .map(this::mapToServingHoursResponse)
        .collect(Collectors.toList());
  }

  // --- Helper methods ---

  /**
   * Parses the incoming alcohol-group names into the persisted enum set.
   *
   * @param groups requested alcohol group names
   * @return parsed alcohol group enum values
   */
  private Set<AlcoholGroup> parseAlcoholGroups(Set<String> groups) {
    Set<AlcoholGroup> result = new HashSet<>();
    for (String g : groups) {
      try {
        result.add(AlcoholGroup.valueOf(g));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid alcohol group: " + g);
      }
    }
    return result;
  }

  /**
   * Maps a bevilling aggregate to the full API response, including associated
   * conditions and serving-hours rows.
   *
   * @param bevilling bevilling entity to convert
   * @return complete bevilling response
   */
  private BevillingResponse mapToResponse(Bevilling bevilling) {
    List<ConditionResponse> conditions = conditionRepository
        .findByBevillingIdOrderByIdAsc(bevilling.getId())
        .stream()
        .map(this::mapToConditionResponse)
        .collect(Collectors.toList());

    List<ServingHoursResponse> servingHours = servingHoursRepository
        .findByBevillingIdOrderByWeekdayAsc(bevilling.getId())
        .stream()
        .map(this::mapToServingHoursResponse)
        .collect(Collectors.toList());

    Set<String> groupNames = bevilling.getAlcoholGroupsAllowed().stream()
        .map(AlcoholGroup::name)
        .collect(Collectors.toSet());

    return new BevillingResponse(
        bevilling.getId(),
        bevilling.getMunicipality(),
        bevilling.getBevillingType().name(),
        bevilling.getValidFrom(),
        bevilling.getValidTo(),
        bevilling.getLicenseNumber(),
        bevilling.getStatus().name(),
        groupNames,
        bevilling.getServingAreaDescription(),
        bevilling.isIndoorAllowed(),
        bevilling.isOutdoorAllowed(),
        bevilling.getStyrerName(),
        bevilling.getStedfortrederName(),
        bevilling.getNotes(),
        conditions,
        servingHours,
        bevilling.getCreatedAt(),
        bevilling.getUpdatedAt()
    );
  }

  /**
   * Maps a bevilling condition entity to its API response DTO.
   *
   * @param condition persisted bevilling condition
   * @return serialized condition response
   */
  private ConditionResponse mapToConditionResponse(BevillingCondition condition) {
    return new ConditionResponse(
        condition.getId(),
        condition.getConditionType().name(),
        condition.getTitle(),
        condition.getDescription(),
        condition.isActive()
    );
  }

  /**
   * Maps a serving-hours entity to its API response DTO.
   *
   * @param hours persisted serving-hours row
   * @return serialized serving-hours response
   */
  private ServingHoursResponse mapToServingHoursResponse(BevillingServingHours hours) {
    return new ServingHoursResponse(
        hours.getId(),
        hours.getWeekday().name(),
        hours.getStartTime(),
        hours.getEndTime(),
        hours.getConsumptionDeadlineMinutesAfterEnd()
    );
  }
}
