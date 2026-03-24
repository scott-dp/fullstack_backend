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

/**
 * Service for aggregating dashboard statistics.
 *
 * <p>Computes key metrics for the authenticated user's organization, including
 * checklist counts, temperature alerts, deviation counts, and unread notifications.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final ChecklistTemplateRepository checklistTemplateRepository;
  private final ChecklistCompletionRepository checklistCompletionRepository;
  private final TemperatureLogRepository temperatureLogRepository;
  private final DeviationRepository deviationRepository;
  private final NotificationRepository notificationRepository;

  /**
   * Computes and returns aggregated dashboard statistics for the given user.
   *
   * @param currentUser the authenticated user whose organization metrics are computed
   * @return dashboard response containing all aggregated statistics
   */
  public DashboardResponse getDashboard(AppUser currentUser) {
    Long orgId = currentUser.getOrganization().getId();
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

    log.info("Computing dashboard for orgId={}, user={}", orgId, currentUser.getUsername());

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
