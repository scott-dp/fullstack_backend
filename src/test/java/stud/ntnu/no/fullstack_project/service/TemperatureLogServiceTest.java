package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for temperature logging and alert classification.
 */

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.temperature.CreateTemperatureLogRequest;
import stud.ntnu.no.fullstack_project.dto.temperature.TemperatureLogResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.notifications.NotificationType;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureLog;
import stud.ntnu.no.fullstack_project.entity.operations.TemperatureStatus;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.operations.TemperatureLogRepository;
import stud.ntnu.no.fullstack_project.service.operations.NotificationService;
import stud.ntnu.no.fullstack_project.service.operations.TemperatureLogService;

@ExtendWith(MockitoExtension.class)
class TemperatureLogServiceTest {

  @Mock
  private TemperatureLogRepository temperatureLogRepository;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private TemperatureLogService temperatureLogService;

  private AppUser testUser;
  private Organization testOrg;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Restaurant");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private TemperatureLog buildLog(Long id, double temp, double min, double max,
                                  TemperatureStatus status, String location) {
    TemperatureLog log = new TemperatureLog();
    log.setId(id);
    log.setOrganization(testOrg);
    log.setLocation(location);
    log.setTemperature(temp);
    log.setMinThreshold(min);
    log.setMaxThreshold(max);
    log.setStatus(status);
    log.setRecordedBy(testUser);
    log.setRecordedAt(LocalDateTime.now());
    log.setComment("Test comment");
    return log;
  }

  // --- createLog tests ---

  @Test
  void createLog_normalTemperature_returnsNormalStatus() {
    CreateTemperatureLogRequest request = new CreateTemperatureLogRequest(
        "Walk-in Fridge", 4.0, 0.0, 8.0, "Within range"
    );

    when(temperatureLogRepository.save(any(TemperatureLog.class))).thenAnswer(invocation -> {
      TemperatureLog saved = invocation.getArgument(0);
      saved.setId(1L);
      saved.setRecordedAt(LocalDateTime.now());
      return saved;
    });

    TemperatureLogResponse response = temperatureLogService.createLog(request, testUser);

    assertNotNull(response);
    assertEquals("NORMAL", response.status());
    assertEquals(4.0, response.temperature());
    assertEquals("Walk-in Fridge", response.location());
    verify(notificationService, never()).createNotification(
        any(), anyString(), anyString(), any(), anyLong(), anyString());
  }

  @Test
  void createLog_temperatureOutsideThreshold_returnsCriticalStatus() {
    CreateTemperatureLogRequest request = new CreateTemperatureLogRequest(
        "Freezer", -25.0, -20.0, -15.0, "Too cold"
    );

    when(temperatureLogRepository.save(any(TemperatureLog.class))).thenAnswer(invocation -> {
      TemperatureLog saved = invocation.getArgument(0);
      saved.setId(2L);
      saved.setRecordedAt(LocalDateTime.now());
      return saved;
    });

    TemperatureLogResponse response = temperatureLogService.createLog(request, testUser);

    assertEquals("CRITICAL", response.status());
    verify(notificationService).createNotification(
        eq(testUser),
        eq("Critical Temperature Alert"),
        anyString(),
        eq(NotificationType.TEMPERATURE_ALERT),
        eq(2L),
        eq("TEMPERATURE_LOG")
    );
  }

  @Test
  void createLog_temperatureNearMinThreshold_returnsWarningStatus() {
    // min=0, max=8; warning zone is temp < min+2=2 or temp > max-2=6
    // temp=1.5 is in warning zone (< 2)
    CreateTemperatureLogRequest request = new CreateTemperatureLogRequest(
        "Fridge", 1.5, 0.0, 8.0, "Near lower limit"
    );

    when(temperatureLogRepository.save(any(TemperatureLog.class))).thenAnswer(invocation -> {
      TemperatureLog saved = invocation.getArgument(0);
      saved.setId(3L);
      saved.setRecordedAt(LocalDateTime.now());
      return saved;
    });

    TemperatureLogResponse response = temperatureLogService.createLog(request, testUser);

    assertEquals("WARNING", response.status());
    verify(notificationService, never()).createNotification(
        any(), anyString(), anyString(), any(), anyLong(), anyString());
  }

  @Test
  void createLog_temperatureNearMaxThreshold_returnsWarningStatus() {
    // min=0, max=8; warning zone is temp > max-2=6
    // temp=6.5 is in warning zone
    CreateTemperatureLogRequest request = new CreateTemperatureLogRequest(
        "Fridge", 6.5, 0.0, 8.0, "Near upper limit"
    );

    when(temperatureLogRepository.save(any(TemperatureLog.class))).thenAnswer(invocation -> {
      TemperatureLog saved = invocation.getArgument(0);
      saved.setId(4L);
      saved.setRecordedAt(LocalDateTime.now());
      return saved;
    });

    TemperatureLogResponse response = temperatureLogService.createLog(request, testUser);

    assertEquals("WARNING", response.status());
  }

  @Test
  void createLog_criticalTemperature_createsNotification() {
    CreateTemperatureLogRequest request = new CreateTemperatureLogRequest(
        "Storage", 12.0, 0.0, 8.0, "Way too high"
    );

    when(temperatureLogRepository.save(any(TemperatureLog.class))).thenAnswer(invocation -> {
      TemperatureLog saved = invocation.getArgument(0);
      saved.setId(5L);
      saved.setRecordedAt(LocalDateTime.now());
      return saved;
    });

    temperatureLogService.createLog(request, testUser);

    verify(notificationService).createNotification(
        eq(testUser),
        eq("Critical Temperature Alert"),
        contains("Storage"),
        eq(NotificationType.TEMPERATURE_ALERT),
        eq(5L),
        eq("TEMPERATURE_LOG")
    );
  }

  // --- getLog tests ---

  @Test
  void getLog_existingLog_returnsResponse() {
    TemperatureLog log = buildLog(1L, 4.0, 0.0, 8.0, TemperatureStatus.NORMAL, "Fridge");
    when(temperatureLogRepository.findById(1L)).thenReturn(Optional.of(log));

    TemperatureLogResponse response = temperatureLogService.getLog(1L);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals(4.0, response.temperature());
    assertEquals("Fridge", response.location());
    assertEquals("NORMAL", response.status());
    assertEquals("testuser", response.recordedByUsername());
  }

  @Test
  void getLog_nonExistentId_throwsIllegalArgumentException() {
    when(temperatureLogRepository.findById(999L)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> temperatureLogService.getLog(999L));
    assertTrue(ex.getMessage().contains("not found"));
  }

  // --- listLogs tests ---

  @Test
  void listLogs_returnsAll_whenNoLocationFilter() {
    TemperatureLog log1 = buildLog(1L, 4.0, 0.0, 8.0, TemperatureStatus.NORMAL, "Fridge");
    TemperatureLog log2 = buildLog(2L, -18.0, -22.0, -16.0, TemperatureStatus.NORMAL, "Freezer");

    when(temperatureLogRepository.findByOrganizationIdOrderByRecordedAtDesc(1L))
        .thenReturn(List.of(log1, log2));

    List<TemperatureLogResponse> result = temperatureLogService.listLogs(1L, null);

    assertEquals(2, result.size());
    verify(temperatureLogRepository).findByOrganizationIdOrderByRecordedAtDesc(1L);
  }

  @Test
  void listLogs_filtersByLocation() {
    TemperatureLog log1 = buildLog(1L, 4.0, 0.0, 8.0, TemperatureStatus.NORMAL, "Fridge");

    when(temperatureLogRepository.findByOrganizationIdAndLocationOrderByRecordedAtDesc(1L, "Fridge"))
        .thenReturn(List.of(log1));

    List<TemperatureLogResponse> result = temperatureLogService.listLogs(1L, "Fridge");

    assertEquals(1, result.size());
    assertEquals("Fridge", result.get(0).location());
    verify(temperatureLogRepository)
        .findByOrganizationIdAndLocationOrderByRecordedAtDesc(1L, "Fridge");
  }

  @Test
  void listLogs_blankLocation_returnsAll() {
    when(temperatureLogRepository.findByOrganizationIdOrderByRecordedAtDesc(1L))
        .thenReturn(List.of());

    List<TemperatureLogResponse> result = temperatureLogService.listLogs(1L, "  ");

    assertEquals(0, result.size());
    verify(temperatureLogRepository).findByOrganizationIdOrderByRecordedAtDesc(1L);
  }

  // --- calculateStatus tests ---

  @Test
  void calculateStatus_tempBelowMin_returnsCritical() {
    assertEquals(TemperatureStatus.CRITICAL,
        temperatureLogService.calculateStatus(-1.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempAboveMax_returnsCritical() {
    assertEquals(TemperatureStatus.CRITICAL,
        temperatureLogService.calculateStatus(9.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempJustAboveMin_returnsWarning() {
    // min=0, min+2=2; temp=1.0 is in warning zone
    assertEquals(TemperatureStatus.WARNING,
        temperatureLogService.calculateStatus(1.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempJustBelowMax_returnsWarning() {
    // max=8, max-2=6; temp=7.0 is in warning zone
    assertEquals(TemperatureStatus.WARNING,
        temperatureLogService.calculateStatus(7.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempWellInRange_returnsNormal() {
    assertEquals(TemperatureStatus.NORMAL,
        temperatureLogService.calculateStatus(4.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempExactlyAtMinPlusTwo_returnsNormal() {
    // min=0, min+2=2; temp=2.0 is NOT < 2 so NORMAL
    assertEquals(TemperatureStatus.NORMAL,
        temperatureLogService.calculateStatus(2.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempExactlyAtMaxMinusTwo_returnsNormal() {
    // max=8, max-2=6; temp=6.0 is NOT > 6 so NORMAL
    assertEquals(TemperatureStatus.NORMAL,
        temperatureLogService.calculateStatus(6.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempExactlyAtMin_returnsCritical() {
    // min=0; temp=0.0 is NOT < 0, but IS < min+2=2 -> WARNING
    // Wait: temp=0.0, 0.0 < 0 is false, 0.0 < 2 is true -> WARNING
    assertEquals(TemperatureStatus.WARNING,
        temperatureLogService.calculateStatus(0.0, 0.0, 8.0));
  }

  @Test
  void calculateStatus_tempExactlyAtMax_returnsCritical() {
    // max=8; temp=8.0, 8.0 > 8 is false, 8.0 > 6 is true -> WARNING
    assertEquals(TemperatureStatus.WARNING,
        temperatureLogService.calculateStatus(8.0, 0.0, 8.0));
  }
}
