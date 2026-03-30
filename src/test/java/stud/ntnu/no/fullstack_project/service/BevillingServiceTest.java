package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for alcohol license retrieval, persistence, and condition updates.
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.bevilling.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.licensing.*;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingConditionRepository;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingRepository;
import stud.ntnu.no.fullstack_project.repository.licensing.BevillingServingHoursRepository;
import stud.ntnu.no.fullstack_project.service.operations.BevillingService;

@ExtendWith(MockitoExtension.class)
class BevillingServiceTest {

  @Mock
  private BevillingRepository bevillingRepository;

  @Mock
  private BevillingConditionRepository conditionRepository;

  @Mock
  private BevillingServingHoursRepository servingHoursRepository;

  @InjectMocks
  private BevillingService bevillingService;

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
    testUser.setUsername("admin");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private Bevilling buildBevilling(Long id, BevillingType type, BevillingStatus status) {
    Bevilling b = new Bevilling();
    b.setId(id);
    b.setOrganization(testOrg);
    b.setMunicipality("Trondheim");
    b.setBevillingType(type);
    b.setValidFrom(LocalDate.of(2025, 1, 1));
    b.setValidTo(LocalDate.of(2027, 12, 31));
    b.setLicenseNumber("SK-2025-001");
    b.setStatus(status);
    b.setAlcoholGroupsAllowed(Set.of(AlcoholGroup.GROUP_1, AlcoholGroup.GROUP_2));
    b.setIndoorAllowed(true);
    b.setOutdoorAllowed(false);
    b.setCreatedAt(LocalDateTime.now());
    b.setUpdatedAt(LocalDateTime.now());
    return b;
  }

  // --- create tests ---

  @Test
  void create_validInput_createsBevillingWithActiveStatus() {
    CreateBevillingRequest request = new CreateBevillingRequest(
        "Trondheim", "SKJENKING", "2025-01-01", "2027-12-31",
        "SK-2025-001", Set.of("GROUP_1", "GROUP_2", "GROUP_3"),
        "Main dining room", true, false, "Ola Nordmann",
        "Kari Nordmann", "Renewed annually"
    );

    when(bevillingRepository.save(any(Bevilling.class))).thenAnswer(invocation -> {
      Bevilling saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });
    when(conditionRepository.findByBevillingIdOrderByIdAsc(10L)).thenReturn(List.of());
    when(servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(10L)).thenReturn(List.of());

    BevillingResponse response = bevillingService.create(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("SKJENKING", response.bevillingType());
    assertEquals("ACTIVE", response.status());
    assertEquals("Trondheim", response.municipality());
    assertEquals(3, response.alcoholGroupsAllowed().size());
    assertTrue(response.indoorAllowed());
    assertFalse(response.outdoorAllowed());
  }

  @Test
  void create_invalidBevillingType_throwsIllegalArgumentException() {
    CreateBevillingRequest request = new CreateBevillingRequest(
        "Trondheim", "INVALID", "2025-01-01", null,
        null, null, null, null, null, null, null, null
    );

    assertThrows(IllegalArgumentException.class,
        () -> bevillingService.create(request, testUser));
  }

  // --- get tests ---

  @Test
  void get_existingId_returnsResponse() {
    Bevilling b = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    when(bevillingRepository.findById(1L)).thenReturn(Optional.of(b));
    when(conditionRepository.findByBevillingIdOrderByIdAsc(1L)).thenReturn(List.of());
    when(servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(1L)).thenReturn(List.of());

    BevillingResponse response = bevillingService.get(1L);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("SKJENKING", response.bevillingType());
  }

  @Test
  void get_nonExistentId_throwsIllegalArgumentException() {
    when(bevillingRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> bevillingService.get(999L));
  }

  // --- list tests ---

  @Test
  void list_returnsAllForOrg() {
    Bevilling b1 = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    Bevilling b2 = buildBevilling(2L, BevillingType.SALG, BevillingStatus.EXPIRED);

    when(bevillingRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(b1, b2));
    when(conditionRepository.findByBevillingIdOrderByIdAsc(anyLong())).thenReturn(List.of());
    when(servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(anyLong()))
        .thenReturn(List.of());

    List<BevillingResponse> result = bevillingService.list(1L);

    assertEquals(2, result.size());
  }

  // --- getCurrent tests ---

  @Test
  void getCurrent_returnsActiveBevilling() {
    Bevilling b = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    when(bevillingRepository.findByOrganizationIdAndStatus(1L, BevillingStatus.ACTIVE))
        .thenReturn(Optional.of(b));
    when(conditionRepository.findByBevillingIdOrderByIdAsc(1L)).thenReturn(List.of());
    when(servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(1L)).thenReturn(List.of());

    BevillingResponse response = bevillingService.getCurrent(1L);

    assertNotNull(response);
    assertEquals("ACTIVE", response.status());
  }

  @Test
  void getCurrent_noActive_returnsNull() {
    when(bevillingRepository.findByOrganizationIdAndStatus(1L, BevillingStatus.ACTIVE))
        .thenReturn(Optional.empty());

    BevillingResponse response = bevillingService.getCurrent(1L);
    assertNull(response);
  }

  // --- update tests ---

  @Test
  void update_statusChange_works() {
    Bevilling b = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    when(bevillingRepository.findById(1L)).thenReturn(Optional.of(b));
    when(bevillingRepository.save(any(Bevilling.class))).thenAnswer(i -> i.getArgument(0));
    when(conditionRepository.findByBevillingIdOrderByIdAsc(1L)).thenReturn(List.of());
    when(servingHoursRepository.findByBevillingIdOrderByWeekdayAsc(1L)).thenReturn(List.of());

    UpdateBevillingRequest request = new UpdateBevillingRequest(
        null, null, null, null, null, "SUSPENDED",
        null, null, null, null, null, null, null
    );

    BevillingResponse response = bevillingService.update(1L, request);

    assertEquals("SUSPENDED", response.status());
  }

  // --- addCondition tests ---

  @Test
  void addCondition_validInput_addsCondition() {
    Bevilling b = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    when(bevillingRepository.findById(1L)).thenReturn(Optional.of(b));
    when(conditionRepository.save(any(BevillingCondition.class))).thenAnswer(invocation -> {
      BevillingCondition saved = invocation.getArgument(0);
      saved.setId(5L);
      return saved;
    });

    CreateConditionRequest request = new CreateConditionRequest(
        "FOOD_REQUIREMENT", "Food must be available",
        "Hot food must be available during all serving hours."
    );

    ConditionResponse response = bevillingService.addCondition(1L, request);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals("FOOD_REQUIREMENT", response.conditionType());
    assertEquals("Food must be available", response.title());
    assertTrue(response.active());
  }

  // --- setServingHours tests ---

  @Test
  void setServingHours_replacesExistingHours() {
    Bevilling b = buildBevilling(1L, BevillingType.SKJENKING, BevillingStatus.ACTIVE);
    when(bevillingRepository.findById(1L)).thenReturn(Optional.of(b));
    doNothing().when(servingHoursRepository).deleteByBevillingId(1L);
    when(servingHoursRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<BevillingServingHours> list = invocation.getArgument(0);
      long id = 1;
      for (BevillingServingHours h : list) {
        h.setId(id++);
      }
      return list;
    });

    List<ServingHoursEntry> entries = List.of(
        new ServingHoursEntry("MON", "11:00", "23:00", 30),
        new ServingHoursEntry("FRI", "11:00", "02:00", 30)
    );

    List<ServingHoursResponse> result = bevillingService.setServingHours(1L, entries);

    assertEquals(2, result.size());
    assertEquals("MON", result.get(0).weekday());
    assertEquals(LocalTime.of(11, 0), result.get(0).startTime());
    verify(servingHoursRepository).deleteByBevillingId(1L);
  }
}
