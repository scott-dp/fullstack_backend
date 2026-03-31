package stud.ntnu.no.fullstack_project.service.operations;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.temperature.CreateTemperatureLogRequest;
import stud.ntnu.no.fullstack_project.dto.temperature.TemperatureLogResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.notifications.NotificationType;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureLog;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;
import stud.ntnu.no.fullstack_project.repository.operations.TemperatureLogRepository;

/**
 * Service for managing temperature logs.
 *
 * <p>Handles recording temperature measurements with automatic threshold-based
 * status calculation, and provides retrieval and filtering capabilities.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureLogService {

  private final TemperatureLogRepository temperatureLogRepository;
  private final NotificationService notificationService;

  /**
   * Records a new temperature measurement and triggers a notification if critical.
   *
   * @param request     the temperature measurement details
   * @param currentUser the authenticated user recording the measurement
   * @return the created temperature log response
   */
  @Transactional
  public TemperatureLogResponse createLog(CreateTemperatureLogRequest request,
      AppUser currentUser) {
    TemperatureStatus status = calculateStatus(
        request.temperature(), request.minThreshold(), request.maxThreshold());

    TemperatureLog tempLog = new TemperatureLog();
    tempLog.setOrganization(currentUser.getOrganization());
    tempLog.setLocation(request.location());
    tempLog.setTemperature(request.temperature());
    tempLog.setMinThreshold(request.minThreshold());
    tempLog.setMaxThreshold(request.maxThreshold());
    tempLog.setStatus(status);
    tempLog.setRecordedBy(currentUser);
    tempLog.setComment(request.comment());

    TemperatureLog saved = temperatureLogRepository.save(tempLog);
    log.info("Temperature log created: location={}, temp={}, status={}",
        saved.getLocation(), saved.getTemperature(), saved.getStatus());

    if (status == TemperatureStatus.CRITICAL) {
      notificationService.createNotification(
          currentUser,
          "Critical Temperature Alert",
          String.format("Temperature at %s is %.1f\u00B0C (thresholds: %.1f-%.1f\u00B0C)",
              request.location(), request.temperature(),
              request.minThreshold(), request.maxThreshold()),
          NotificationType.TEMPERATURE_ALERT,
          saved.getId(),
          "TEMPERATURE_LOG"
      );
    }

    return mapToResponse(saved);
  }

  /**
   * Retrieves a temperature log by its ID.
   *
   * @param id the temperature log identifier
   * @return the temperature log response
   */
  public TemperatureLogResponse getLog(Long id) {
    TemperatureLog tempLog = temperatureLogRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Temperature log not found with id: " + id));
    return mapToResponse(tempLog);
  }

  /**
   * Lists temperature logs for an organization, optionally filtered by location.
   *
   * @param organizationId the organization identifier
   * @param location       optional location filter
   * @return list of matching temperature log responses ordered by most recent first
   */
  public List<TemperatureLogResponse> listLogs(Long organizationId, String location) {
    List<TemperatureLog> logs;

    if (location != null && !location.isBlank()) {
      logs = temperatureLogRepository.findByOrganizationIdAndLocationOrderByRecordedAtDesc(
          organizationId, location);
    } else {
      logs = temperatureLogRepository.findByOrganizationIdOrderByRecordedAtDesc(organizationId);
    }

    return logs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Calculates the temperature status based on the value and thresholds.
   *
   * <p>Returns CRITICAL if the temperature is outside the thresholds, WARNING if
   * within 2 degrees of a threshold boundary, and NORMAL otherwise.</p>
   *
   * @param temp the measured temperature
   * @param min  the minimum acceptable threshold
   * @param max  the maximum acceptable threshold
   * @return the computed temperature status
   */
  public TemperatureStatus calculateStatus(double temp, double min, double max) {
    if (temp < min || temp > max) {
      return TemperatureStatus.CRITICAL;
    }
    if (temp < min + 2 || temp > max - 2) {
      return TemperatureStatus.WARNING;
    }
    return TemperatureStatus.NORMAL;
  }

  /**
   * Maps a temperature log entity to its response DTO.
   *
   * @param tempLog the temperature log entity
   * @return the temperature log response DTO
   */
  private TemperatureLogResponse mapToResponse(TemperatureLog tempLog) {
    return new TemperatureLogResponse(
        tempLog.getId(),
        tempLog.getLocation(),
        tempLog.getTemperature(),
        tempLog.getMinThreshold(),
        tempLog.getMaxThreshold(),
        tempLog.getStatus().name(),
        tempLog.getRecordedBy().getUsername(),
        tempLog.getRecordedAt(),
        tempLog.getComment()
    );
  }
}
