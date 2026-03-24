package stud.ntnu.no.fullstack_project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.dto.dashboard.DashboardResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.DeviationStatus;
import stud.ntnu.no.fullstack_project.entity.TemperatureStatus;
import stud.ntnu.no.fullstack_project.repository.ChecklistCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.ChecklistTemplateRepository;
import stud.ntnu.no.fullstack_project.repository.DeviationRepository;
import stud.ntnu.no.fullstack_project.repository.NotificationRepository;
import stud.ntnu.no.fullstack_project.repository.TemperatureLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final ChecklistTemplateRepository checklistTemplateRepository;
  private final ChecklistCompletionRepository checklistCompletionRepository;
  private final TemperatureLogRepository temperatureLogRepository;
  private final DeviationRepository deviationRepository;
  private final NotificationRepository notificationRepository;

  public DashboardResponse getDashboard(AppUser currentUser) {
    Long orgId = currentUser.getOrganization().getId();
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

    long totalChecklistTemplates = checklistTemplateRepository
        .findByOrganizationIdAndActiveTrue(orgId).size();

    long checklistsCompletedToday = checklistCompletionRepository
        .countByTemplateOrganizationIdAndCompletedAtAfter(orgId, startOfToday);

    long temperatureAlertsToday =
        temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
            orgId, TemperatureStatus.WARNING, startOfToday)
        + temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
            orgId, TemperatureStatus.CRITICAL, startOfToday);

    long openDeviations = deviationRepository
        .countByOrganizationIdAndStatus(orgId, DeviationStatus.OPEN);

    long inProgressDeviations = deviationRepository
        .countByOrganizationIdAndStatus(orgId, DeviationStatus.IN_PROGRESS);

    long unreadNotifications = notificationRepository
        .countByUserIdAndReadFalse(currentUser.getId());

    return new DashboardResponse(
        totalChecklistTemplates,
        checklistsCompletedToday,
        temperatureAlertsToday,
        openDeviations,
        inProgressDeviations,
        unreadNotifications
    );
  }
}
