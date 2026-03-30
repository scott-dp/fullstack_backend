package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for dashboard aggregation and role-aware dashboard data.
 */

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.dashboard.DashboardResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.ChecklistTemplate;
import stud.ntnu.no.fullstack_project.entity.operations.DeviationStatus;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.operations.ChecklistCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.operations.ChecklistTemplateRepository;
import stud.ntnu.no.fullstack_project.repository.operations.DeviationRepository;
import stud.ntnu.no.fullstack_project.repository.operations.NotificationRepository;
import stud.ntnu.no.fullstack_project.repository.operations.TemperatureLogRepository;
import stud.ntnu.no.fullstack_project.service.operations.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private ChecklistTemplateRepository checklistTemplateRepository;

  @Mock
  private ChecklistCompletionRepository checklistCompletionRepository;

  @Mock
  private TemperatureLogRepository temperatureLogRepository;

  @Mock
  private DeviationRepository deviationRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private DashboardService dashboardService;

  private AppUser testUser;
  private Organization testOrg;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);
  }

  // --- getDashboard tests ---

  @Test
  void getDashboard_returnsAggregatedStats() {
    when(appUserRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

    // Set up mock returns
    ChecklistTemplate t1 = new ChecklistTemplate();
    t1.setId(1L);
    ChecklistTemplate t2 = new ChecklistTemplate();
    t2.setId(2L);
    ChecklistTemplate t3 = new ChecklistTemplate();
    t3.setId(3L);

    when(checklistTemplateRepository.findByOrganizationIdAndActiveTrue(1L))
        .thenReturn(List.of(t1, t2, t3));
    when(checklistCompletionRepository.countByTemplateOrganizationIdAndCompletedAtAfter(
        eq(1L), any(LocalDateTime.class)))
        .thenReturn(7L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.WARNING), any(LocalDateTime.class)))
        .thenReturn(2L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.CRITICAL), any(LocalDateTime.class)))
        .thenReturn(1L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.OPEN))
        .thenReturn(4L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.IN_PROGRESS))
        .thenReturn(2L);
    when(notificationRepository.countByUserIdAndReadFalse(1L))
        .thenReturn(10L);

    DashboardResponse response = dashboardService.getDashboard(testUser);

    assertNotNull(response);
    assertTrue(response.organizationAssigned());
    assertEquals("Test Org", response.organizationName());
    assertNull(response.message());
    assertEquals(3L, response.totalChecklistTemplates());
    assertEquals(7L, response.checklistsCompletedToday());
    assertEquals(3L, response.temperatureAlertsToday()); // 2 WARNING + 1 CRITICAL
    assertEquals(4L, response.openDeviations());
    assertEquals(2L, response.inProgressDeviations());
    assertEquals(10L, response.unreadNotifications());
  }

  @Test
  void getDashboard_handlesZeroCountsCorrectly() {
    when(appUserRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

    when(checklistTemplateRepository.findByOrganizationIdAndActiveTrue(1L))
        .thenReturn(List.of());
    when(checklistCompletionRepository.countByTemplateOrganizationIdAndCompletedAtAfter(
        eq(1L), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.WARNING), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.CRITICAL), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.OPEN))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.IN_PROGRESS))
        .thenReturn(0L);
    when(notificationRepository.countByUserIdAndReadFalse(1L))
        .thenReturn(0L);

    DashboardResponse response = dashboardService.getDashboard(testUser);

    assertNotNull(response);
    assertTrue(response.organizationAssigned());
    assertEquals(0L, response.totalChecklistTemplates());
    assertEquals(0L, response.checklistsCompletedToday());
    assertEquals(0L, response.temperatureAlertsToday());
    assertEquals(0L, response.openDeviations());
    assertEquals(0L, response.inProgressDeviations());
    assertEquals(0L, response.unreadNotifications());
  }

  @Test
  void getDashboard_correctlyCallsAllRepositories() {
    when(appUserRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

    when(checklistTemplateRepository.findByOrganizationIdAndActiveTrue(1L))
        .thenReturn(List.of());
    when(checklistCompletionRepository.countByTemplateOrganizationIdAndCompletedAtAfter(
        eq(1L), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.WARNING), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.CRITICAL), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.OPEN))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(1L, DeviationStatus.IN_PROGRESS))
        .thenReturn(0L);
    when(notificationRepository.countByUserIdAndReadFalse(1L))
        .thenReturn(0L);

    dashboardService.getDashboard(testUser);

    verify(checklistTemplateRepository).findByOrganizationIdAndActiveTrue(1L);
    verify(checklistCompletionRepository).countByTemplateOrganizationIdAndCompletedAtAfter(
        eq(1L), any(LocalDateTime.class));
    verify(temperatureLogRepository).countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.WARNING), any(LocalDateTime.class));
    verify(temperatureLogRepository).countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(1L), eq(TemperatureStatus.CRITICAL), any(LocalDateTime.class));
    verify(deviationRepository).countByOrganizationIdAndStatus(1L, DeviationStatus.OPEN);
    verify(deviationRepository).countByOrganizationIdAndStatus(1L, DeviationStatus.IN_PROGRESS);
    verify(notificationRepository).countByUserIdAndReadFalse(1L);
  }

  @Test
  void getDashboard_usesCorrectOrganizationId() {
    Organization otherOrg = new Organization();
    otherOrg.setId(42L);
    otherOrg.setName("Other Org");
    otherOrg.setType(OrganizationType.BAR);

    AppUser otherUser = new AppUser();
    otherUser.setId(5L);
    otherUser.setUsername("otheruser");
    otherUser.setPassword("encoded");
    otherUser.setOrganization(otherOrg);

    when(checklistTemplateRepository.findByOrganizationIdAndActiveTrue(42L))
        .thenReturn(List.of());
    when(checklistCompletionRepository.countByTemplateOrganizationIdAndCompletedAtAfter(
        eq(42L), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(42L), eq(TemperatureStatus.WARNING), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(temperatureLogRepository.countByOrganizationIdAndStatusAndRecordedAtAfter(
        eq(42L), eq(TemperatureStatus.CRITICAL), any(LocalDateTime.class)))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(42L, DeviationStatus.OPEN))
        .thenReturn(0L);
    when(deviationRepository.countByOrganizationIdAndStatus(42L, DeviationStatus.IN_PROGRESS))
        .thenReturn(0L);
    when(notificationRepository.countByUserIdAndReadFalse(5L))
        .thenReturn(0L);
    when(appUserRepository.findById(5L)).thenReturn(java.util.Optional.of(otherUser));

    dashboardService.getDashboard(otherUser);

    verify(checklistTemplateRepository).findByOrganizationIdAndActiveTrue(42L);
    verify(deviationRepository).countByOrganizationIdAndStatus(42L, DeviationStatus.OPEN);
    verify(notificationRepository).countByUserIdAndReadFalse(5L);
  }

  @Test
  void getDashboard_returnsEmptyStateWhenUserHasNoOrganization() {
    testUser.setOrganization(null);
    when(appUserRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
    when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(3L);

    DashboardResponse response = dashboardService.getDashboard(testUser);

    assertFalse(response.organizationAssigned());
    assertNull(response.organizationName());
    assertEquals("You have not joined an organization yet. Accept an invitation to get started.", response.message());
    assertEquals(0L, response.totalChecklistTemplates());
    assertEquals(0L, response.checklistsCompletedToday());
    assertEquals(0L, response.temperatureAlertsToday());
    assertEquals(0L, response.openDeviations());
    assertEquals(0L, response.inProgressDeviations());
    assertEquals(3L, response.unreadNotifications());
    verifyNoInteractions(checklistTemplateRepository, checklistCompletionRepository, temperatureLogRepository, deviationRepository);
  }
}
