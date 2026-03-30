package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.dashboard.DashboardResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.DeviationStatus;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.operations.ChecklistCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.operations.ChecklistTemplateRepository;
import stud.ntnu.no.fullstack_project.repository.operations.DeviationRepository;
import stud.ntnu.no.fullstack_project.repository.operations.NotificationRepository;
import stud.ntnu.no.fullstack_project.repository.operations.TemperatureLogRepository;

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

  private final AppUserRepository appUserRepository;
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
  @Transactional(readOnly = true)
  public DashboardResponse getDashboard(AppUser currentUser) {
    AppUser managedUser = appUserRepository.findById(currentUser.getId())
        .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));

    long unreadNotifications = notificationRepository
        .countByUserIdAndReadFalse(managedUser.getId());

    if (managedUser.getOrganization() == null) {
      log.info("Returning empty dashboard for user={} because no organization is assigned", managedUser.getUsername());
      return new DashboardResponse(
          false,
          null,
          "You have not joined an organization yet. Accept an invitation to get started.",
          0,
          0,
          0,
          0,
          0,
          unreadNotifications
      );
    }

    Long orgId = managedUser.getOrganization().getId();
    String organizationName = managedUser.getOrganization().getName();
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

    log.info("Computing dashboard for orgId={}, user={}", orgId, managedUser.getUsername());

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

    return new DashboardResponse(
        true,
        organizationName,
        null,
        totalChecklistTemplates,
        checklistsCompletedToday,
        temperatureAlertsToday,
        openDeviations,
        inProgressDeviations,
        unreadNotifications
    );
  }
}
