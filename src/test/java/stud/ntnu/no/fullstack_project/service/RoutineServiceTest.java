package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for routine CRUD, reviews, archiving, and restoration behavior.
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
import stud.ntnu.no.fullstack_project.dto.routine.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.auth.ResponsibleRole;
import stud.ntnu.no.fullstack_project.entity.operations.*;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.operations.ChecklistTemplateRepository;
import stud.ntnu.no.fullstack_project.repository.operations.RoutineRepository;
import stud.ntnu.no.fullstack_project.repository.operations.RoutineReviewRepository;
import stud.ntnu.no.fullstack_project.service.operations.RoutineService;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

  @Mock
  private RoutineRepository routineRepository;

  @Mock
  private RoutineReviewRepository routineReviewRepository;

  @Mock
  private ChecklistTemplateRepository checklistTemplateRepository;

  @InjectMocks
  private RoutineService routineService;

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

  private Routine buildRoutine(Long id, String name) {
    Routine r = new Routine();
    r.setId(id);
    r.setOrganization(testOrg);
    r.setName(name);
    r.setModuleType(ModuleType.IK_MAT);
    r.setCategory(RoutineCategory.TEMPERATURE);
    r.setResponsibleRole(ResponsibleRole.STAFF);
    r.setFrequencyType(FrequencyType.DAILY);
    r.setActive(true);
    r.setVersionNumber(1);
    r.setCreatedBy(testUser);
    r.setCreatedAt(LocalDateTime.now());
    r.setUpdatedAt(LocalDateTime.now());
    return r;
  }

  @Test
  void createRoutine_validInput_createsRoutine() {
    CreateRoutineRequest request = new CreateRoutineRequest(
        "Morning fridge check", "IK_MAT", "TEMPERATURE",
        "Check all fridges", "Food safety", "STAFF", "DAILY",
        "Step 1: open fridge", "Temp above 4C", "Adjust thermostat",
        "Photo of thermometer", null, 30
    );

    when(routineRepository.save(any(Routine.class))).thenAnswer(inv -> {
      Routine saved = inv.getArgument(0);
      saved.setId(1L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });

    RoutineResponse response = routineService.createRoutine(request, testUser);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Morning fridge check", response.name());
    assertEquals("IK_MAT", response.moduleType());
    assertEquals("TEMPERATURE", response.category());
    assertEquals("DAILY", response.frequencyType());
    assertTrue(response.active());
    assertEquals(1, response.versionNumber());
  }

  @Test
  void createRoutine_invalidModuleType_throwsException() {
    CreateRoutineRequest request = new CreateRoutineRequest(
        "Test", "INVALID", "TEMPERATURE", null, null, "STAFF", "DAILY",
        null, null, null, null, null, null
    );

    assertThrows(IllegalArgumentException.class,
        () -> routineService.createRoutine(request, testUser));
  }

  @Test
  void getRoutine_existingId_returnsRoutine() {
    Routine routine = buildRoutine(1L, "Test Routine");
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));

    RoutineResponse response = routineService.getRoutine(1L);

    assertEquals(1L, response.id());
    assertEquals("Test Routine", response.name());
  }

  @Test
  void getRoutine_nonExistentId_throwsException() {
    when(routineRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> routineService.getRoutine(999L));
  }

  @Test
  void listRoutines_noFilter_returnsAll() {
    Routine r1 = buildRoutine(1L, "Routine 1");
    Routine r2 = buildRoutine(2L, "Routine 2");
    when(routineRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(r1, r2));

    List<RoutineResponse> result = routineService.listRoutines(1L, null, null, null);

    assertEquals(2, result.size());
  }

  @Test
  void listRoutines_moduleTypeFilter_works() {
    Routine r1 = buildRoutine(1L, "Mat Routine");
    when(routineRepository.findByOrganizationIdAndModuleTypeOrderByCreatedAtDesc(1L, ModuleType.IK_MAT))
        .thenReturn(List.of(r1));

    List<RoutineResponse> result = routineService.listRoutines(1L, "IK_MAT", null, null);

    assertEquals(1, result.size());
    assertEquals("IK_MAT", result.get(0).moduleType());
  }

  @Test
  void updateRoutine_incrementsVersion() {
    Routine routine = buildRoutine(1L, "Old Name");
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
    when(routineRepository.save(any(Routine.class))).thenAnswer(i -> i.getArgument(0));

    UpdateRoutineRequest request = new UpdateRoutineRequest(
        "New Name", null, null, null, null, null, null,
        null, null, null, null, null, null
    );

    RoutineResponse response = routineService.updateRoutine(1L, request, testUser);

    assertEquals("New Name", response.name());
    assertEquals(2, response.versionNumber());
  }

  @Test
  void archiveRoutine_setsActiveToFalse() {
    Routine routine = buildRoutine(1L, "To Archive");
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
    when(routineRepository.save(any(Routine.class))).thenAnswer(i -> i.getArgument(0));

    RoutineResponse response = routineService.archiveRoutine(1L);

    assertFalse(response.active());
  }

  @Test
  void unarchiveRoutine_setsActiveToTrue() {
    Routine routine = buildRoutine(1L, "To Restore");
    routine.setActive(false);
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
    when(routineRepository.save(any(Routine.class))).thenAnswer(i -> i.getArgument(0));

    RoutineResponse response = routineService.unarchiveRoutine(1L);

    assertTrue(response.active());
  }

  @Test
  void deleteRoutine_removesReviewsAndRoutine() {
    Routine routine = buildRoutine(1L, "To Delete");
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));

    routineService.deleteRoutine(1L);

    verify(routineReviewRepository).deleteByRoutineId(1L);
    verify(routineRepository).delete(routine);
  }

  @Test
  void reviewRoutine_createsReviewAndUpdatesLastReviewed() {
    Routine routine = buildRoutine(1L, "Review Me");
    routine.setReviewIntervalDays(30);
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
    when(routineRepository.save(any(Routine.class))).thenAnswer(i -> i.getArgument(0));
    when(routineReviewRepository.save(any(RoutineReview.class))).thenAnswer(inv -> {
      RoutineReview saved = inv.getArgument(0);
      saved.setId(10L);
      return saved;
    });

    ReviewRoutineRequest request = new ReviewRoutineRequest("All good");

    RoutineReviewResponse response = routineService.reviewRoutine(1L, request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("admin", response.reviewedByUsername());
    assertEquals("All good", response.notes());
    assertNotNull(response.nextReviewAt());
    assertNotNull(routine.getLastReviewedAt());
  }

  @Test
  void getRoutineHistory_returnsReviews() {
    Routine routine = buildRoutine(1L, "History Routine");
    when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));

    RoutineReview review = new RoutineReview();
    review.setId(1L);
    review.setRoutine(routine);
    review.setReviewedBy(testUser);
    review.setReviewedAt(LocalDateTime.now());
    review.setNotes("First review");

    when(routineReviewRepository.findByRoutineIdOrderByReviewedAtDesc(1L))
        .thenReturn(List.of(review));

    List<RoutineReviewResponse> result = routineService.getRoutineHistory(1L);

    assertEquals(1, result.size());
    assertEquals("First review", result.get(0).notes());
  }
}
