package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.incident.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.licensing.AlcoholIncident;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentSeverity;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentStatus;
import stud.ntnu.no.fullstack_project.entity.operations.IncidentType;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.operations.AlcoholIncidentRepository;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.service.operations.AlcoholIncidentService;

@ExtendWith(MockitoExtension.class)
class AlcoholIncidentServiceTest {

  @Mock
  private AlcoholIncidentRepository alcoholIncidentRepository;

  @Mock
  private AppUserRepository appUserRepository;

  @InjectMocks
  private AlcoholIncidentService alcoholIncidentService;

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
    testUser.setUsername("reporter");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private AlcoholIncident buildIncident(Long id, IncidentType type, IncidentStatus status) {
    AlcoholIncident incident = new AlcoholIncident();
    incident.setId(id);
    incident.setOrganization(testOrg);
    incident.setOccurredAt(LocalDateTime.now());
    incident.setReportedBy(testUser);
    incident.setIncidentType(type);
    incident.setSeverity(IncidentSeverity.MEDIUM);
    incident.setDescription("Test incident description");
    incident.setFollowUpRequired(false);
    incident.setStatus(status);
    incident.setCreatedAt(LocalDateTime.now());
    incident.setUpdatedAt(LocalDateTime.now());
    return incident;
  }

  // --- create tests ---

  @Test
  void create_validInput_createsIncidentWithOpenStatus() {
    CreateAlcoholIncidentRequest request = new CreateAlcoholIncidentRequest(
        "2026-03-25T22:30:00",
        "Evening Shift",
        "Bar Area",
        "AGE_DOUBT_REFUSAL",
        "MEDIUM",
        "Customer could not provide valid ID.",
        "Refused service.",
        false,
        null,
        null,
        null
    );

    when(alcoholIncidentRepository.save(any(AlcoholIncident.class))).thenAnswer(invocation -> {
      AlcoholIncident saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });

    AlcoholIncidentResponse response = alcoholIncidentService.create(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("AGE_DOUBT_REFUSAL", response.incidentType());
    assertEquals("MEDIUM", response.severity());
    assertEquals("OPEN", response.status());
    assertEquals("reporter", response.reportedByUsername());
    assertEquals("Bar Area", response.locationArea());
    assertNull(response.assignedToId());
    assertNull(response.assignedToUsername());
    assertNull(response.closedByUsername());
  }

  @Test
  void create_invalidIncidentType_throwsIllegalArgumentException() {
    CreateAlcoholIncidentRequest request = new CreateAlcoholIncidentRequest(
        "2026-03-25T22:30:00", null, null, "INVALID_TYPE", "MEDIUM",
        "Desc", null, false, null, null, null
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> alcoholIncidentService.create(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid incident type"));
  }

  @Test
  void create_invalidSeverity_throwsIllegalArgumentException() {
    CreateAlcoholIncidentRequest request = new CreateAlcoholIncidentRequest(
        "2026-03-25T22:30:00", null, null, "AGE_DOUBT_REFUSAL", "INVALID_SEV",
        "Desc", null, false, null, null, null
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> alcoholIncidentService.create(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid severity"));
  }

  // --- list tests ---

  @Test
  void list_noFilter_returnsAllForOrg() {
    AlcoholIncident i1 = buildIncident(1L, IncidentType.AGE_DOUBT_REFUSAL, IncidentStatus.OPEN);
    AlcoholIncident i2 = buildIncident(2L, IncidentType.INTOXICATION_REFUSAL,
        IncidentStatus.CLOSED);
    i2.setClosedBy(testUser);
    i2.setClosedAt(LocalDateTime.now());

    when(alcoholIncidentRepository.findByOrganizationIdOrderByOccurredAtDesc(1L))
        .thenReturn(List.of(i1, i2));

    List<AlcoholIncidentResponse> result = alcoholIncidentService.list(1L, null, null);

    assertEquals(2, result.size());
    verify(alcoholIncidentRepository).findByOrganizationIdOrderByOccurredAtDesc(1L);
  }

  @Test
  void list_statusFilter_works() {
    AlcoholIncident i1 = buildIncident(1L, IncidentType.AGE_DOUBT_REFUSAL, IncidentStatus.OPEN);
    when(alcoholIncidentRepository.findByOrganizationIdAndStatus(1L, IncidentStatus.OPEN))
        .thenReturn(List.of(i1));

    List<AlcoholIncidentResponse> result = alcoholIncidentService.list(1L, "OPEN", null);

    assertEquals(1, result.size());
    assertEquals("OPEN", result.get(0).status());
  }

  @Test
  void list_typeFilter_works() {
    AlcoholIncident i1 = buildIncident(1L, IncidentType.AGE_DOUBT_REFUSAL, IncidentStatus.OPEN);
    AlcoholIncident i2 = buildIncident(2L, IncidentType.INTOXICATION_REFUSAL,
        IncidentStatus.OPEN);

    when(alcoholIncidentRepository.findByOrganizationIdOrderByOccurredAtDesc(1L))
        .thenReturn(List.of(i1, i2));

    List<AlcoholIncidentResponse> result = alcoholIncidentService.list(
        1L, null, "AGE_DOUBT_REFUSAL");

    assertEquals(1, result.size());
    assertEquals("AGE_DOUBT_REFUSAL", result.get(0).incidentType());
  }

  @Test
  void list_invalidStatus_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> alcoholIncidentService.list(1L, "INVALID_STATUS", null));
  }

  // --- close tests ---

  @Test
  void close_setsClosedFieldsAndStatus() {
    AlcoholIncident incident = buildIncident(1L, IncidentType.GUEST_REMOVED,
        IncidentStatus.OPEN);
    when(alcoholIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
    when(alcoholIncidentRepository.save(any(AlcoholIncident.class)))
        .thenAnswer(i -> i.getArgument(0));

    CloseIncidentRequest request = new CloseIncidentRequest("No further action needed.");

    AlcoholIncidentResponse response = alcoholIncidentService.close(1L, request, testUser);

    assertEquals("CLOSED", response.status());
    assertNotNull(response.closedAt());
    assertEquals("reporter", response.closedByUsername());
    assertTrue(response.immediateActionTaken().contains("Closing notes"));
  }

  @Test
  void close_nonExistentId_throwsIllegalArgumentException() {
    when(alcoholIncidentRepository.findById(999L)).thenReturn(Optional.empty());

    CloseIncidentRequest request = new CloseIncidentRequest("Notes");

    assertThrows(IllegalArgumentException.class,
        () -> alcoholIncidentService.close(999L, request, testUser));
  }

  // --- report tests ---

  @Test
  void report_returnsSummaryWithCorrectCounts() {
    AlcoholIncident i1 = buildIncident(1L, IncidentType.AGE_DOUBT_REFUSAL, IncidentStatus.OPEN);
    AlcoholIncident i2 = buildIncident(2L, IncidentType.AGE_DOUBT_REFUSAL,
        IncidentStatus.CLOSED);
    AlcoholIncident i3 = buildIncident(3L, IncidentType.INTOXICATION_REFUSAL,
        IncidentStatus.OPEN);

    when(alcoholIncidentRepository.findByOrganizationIdOrderByOccurredAtDesc(1L))
        .thenReturn(List.of(i1, i2, i3));
    when(alcoholIncidentRepository.countByOrganizationIdAndStatus(1L, IncidentStatus.OPEN))
        .thenReturn(2L);
    when(alcoholIncidentRepository.countByOrganizationIdAndStatus(1L, IncidentStatus.CLOSED))
        .thenReturn(1L);

    IncidentReportResponse report = alcoholIncidentService.report(1L);

    assertEquals(3, report.totalIncidents());
    assertEquals(2, report.openCount());
    assertEquals(1, report.closedCount());
    assertEquals(2, report.byType().size());
    assertEquals(2L, report.byType().get("AGE_DOUBT_REFUSAL"));
    assertEquals(1L, report.byType().get("INTOXICATION_REFUSAL"));
  }
}
