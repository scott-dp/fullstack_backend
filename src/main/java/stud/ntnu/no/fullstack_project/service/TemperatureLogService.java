package stud.ntnu.no.fullstack_project.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.temperature.CreateTemperatureLogRequest;
import stud.ntnu.no.fullstack_project.dto.temperature.TemperatureLogResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.NotificationType;
import stud.ntnu.no.fullstack_project.entity.TemperatureLog;
import stud.ntnu.no.fullstack_project.entity.TemperatureStatus;
import stud.ntnu.no.fullstack_project.repository.TemperatureLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureLogService {

  private final TemperatureLogRepository temperatureLogRepository;
  private final NotificationService notificationService;

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

  public TemperatureLogResponse getLog(Long id) {
    TemperatureLog tempLog = temperatureLogRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Temperature log not found with id: " + id));
    return mapToResponse(tempLog);
  }

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

  TemperatureStatus calculateStatus(double temp, double min, double max) {
    if (temp < min || temp > max) {
      return TemperatureStatus.CRITICAL;
    }
    if (temp < min + 2 || temp > max - 2) {
      return TemperatureStatus.WARNING;
    }
    return TemperatureStatus.NORMAL;
  }

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
