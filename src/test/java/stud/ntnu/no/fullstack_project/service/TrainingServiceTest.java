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
import stud.ntnu.no.fullstack_project.dto.training.*;
import stud.ntnu.no.fullstack_project.entity.*;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingAssignmentRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.TrainingTemplateRepository;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

  @Mock
  private TrainingTemplateRepository trainingTemplateRepository;

  @Mock
  private TrainingAssignmentRepository trainingAssignmentRepository;

  @Mock
  private TrainingCompletionRepository trainingCompletionRepository;

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private TrainingService trainingService;

  private AppUser testUser;
  private AppUser staffUser;
  private Organization testOrg;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("manager");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);

    staffUser = new AppUser();
    staffUser.setId(2L);
    staffUser.setUsername("staff");
    staffUser.setPassword("encoded");
    staffUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private TrainingTemplate buildTemplate(Long id, String title, ModuleType moduleType,
      TrainingCategory category) {
    TrainingTemplate template = new TrainingTemplate();
    template.setId(id);
    template.setOrganization(testOrg);
    template.setTitle(title);
    template.setModuleType(moduleType);
    template.setCategory(category);
    template.setRequiredForRole(ResponsibleRole.ALL);
    template.setMandatory(true);
    template.setValidityDays(365);
    template.setAcknowledgmentRequired(true);
    template.setActive(true);
    template.setCreatedAt(LocalDateTime.now());
    template.setUpdatedAt(LocalDateTime.now());
    return template;
  }

  private TrainingAssignment buildAssignment(Long id, TrainingTemplate template,
      AppUser assignee, AppUser assignedBy) {
    TrainingAssignment assignment = new TrainingAssignment();
    assignment.setId(id);
    assignment.setTrainingTemplate(template);
    assignment.setOrganization(testOrg);
    assignment.setAssigneeUser(assignee);
    assignment.setAssignedBy(assignedBy);
    assignment.setAssignedAt(LocalDateTime.now());
    assignment.setStatus(TrainingAssignmentStatus.ASSIGNED);
    return assignment;
  }

  // --- createTemplate tests ---

  @Test
  void createTemplate_validInput_createsTemplate() {
    CreateTrainingTemplateRequest request = new CreateTrainingTemplateRequest(
        "Basic food hygiene", "IK_MAT", "FOOD_HYGIENE",
        "Description", "Content text", "ALL",
        true, 365, true
    );

    when(trainingTemplateRepository.save(any(TrainingTemplate.class))).thenAnswer(invocation -> {
      TrainingTemplate saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });

    TrainingTemplateResponse response = trainingService.createTemplate(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("Basic food hygiene", response.title());
    assertEquals("IK_MAT", response.moduleType());
    assertEquals("FOOD_HYGIENE", response.category());
    assertEquals("ALL", response.requiredForRole());
    assertTrue(response.isMandatory());
    assertEquals(365, response.validityDays());
    assertTrue(response.acknowledgmentRequired());
    assertTrue(response.active());
  }

  @Test
  void createTemplate_invalidModuleType_throwsIllegalArgumentException() {
    CreateTrainingTemplateRequest request = new CreateTrainingTemplateRequest(
        "Test", "INVALID_TYPE", "FOOD_HYGIENE",
        null, null, "ALL", false, null, false
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> trainingService.createTemplate(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid module type"));
  }

  @Test
  void createTemplate_invalidCategory_throwsIllegalArgumentException() {
    CreateTrainingTemplateRequest request = new CreateTrainingTemplateRequest(
        "Test", "IK_MAT", "INVALID_CAT",
        null, null, "ALL", false, null, false
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> trainingService.createTemplate(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid category"));
  }

  @Test
  void createTemplate_invalidRole_throwsIllegalArgumentException() {
    CreateTrainingTemplateRequest request = new CreateTrainingTemplateRequest(
        "Test", "IK_MAT", "FOOD_HYGIENE",
        null, null, "INVALID_ROLE", false, null, false
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> trainingService.createTemplate(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid required role"));
  }

  // --- getTemplate tests ---

  @Test
  void getTemplate_existingId_returnsTemplate() {
    TrainingTemplate template = buildTemplate(5L, "Food Hygiene",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    when(trainingTemplateRepository.findById(5L)).thenReturn(Optional.of(template));

    TrainingTemplateResponse response = trainingService.getTemplate(5L);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals("Food Hygiene", response.title());
  }

  @Test
  void getTemplate_nonExistentId_throwsIllegalArgumentException() {
    when(trainingTemplateRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> trainingService.getTemplate(999L));
  }

  // --- listTemplates tests ---

  @Test
  void listTemplates_returnsAllForOrg() {
    TrainingTemplate t1 = buildTemplate(1L, "Template 1",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    TrainingTemplate t2 = buildTemplate(2L, "Template 2",
        ModuleType.IK_ALKOHOL, TrainingCategory.AGE_CONTROL);

    when(trainingTemplateRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(t1, t2));

    List<TrainingTemplateResponse> result = trainingService.listTemplates(1L);

    assertEquals(2, result.size());
    verify(trainingTemplateRepository).findByOrganizationIdOrderByCreatedAtDesc(1L);
  }

  // --- assignTraining tests ---

  @Test
  void assignTraining_validInput_createsAssignments() {
    TrainingTemplate template = buildTemplate(1L, "Food Hygiene",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    when(trainingTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
    when(appUserRepository.findById(2L)).thenReturn(Optional.of(staffUser));
    when(trainingAssignmentRepository.save(any(TrainingAssignment.class)))
        .thenAnswer(invocation -> {
          TrainingAssignment saved = invocation.getArgument(0);
          saved.setId(100L);
          saved.setAssignedAt(LocalDateTime.now());
          return saved;
        });

    AssignTrainingRequest request = new AssignTrainingRequest(
        List.of(2L), "2025-06-01T00:00:00"
    );

    List<TrainingAssignmentResponse> responses =
        trainingService.assignTraining(1L, request, testUser);

    assertEquals(1, responses.size());
    assertEquals(100L, responses.get(0).id());
    assertEquals("staff", responses.get(0).assigneeUsername());
    assertEquals("manager", responses.get(0).assignedByUsername());
    assertEquals("ASSIGNED", responses.get(0).status());
    verify(trainingAssignmentRepository).save(any(TrainingAssignment.class));
    verify(notificationService).createNotification(
        eq(staffUser),
        eq("Training Assigned"),
        eq("You have been assigned training: Food Hygiene"),
        eq(NotificationType.TRAINING_ASSIGNED),
        eq(100L),
        eq("TRAINING_ASSIGNMENT"));
  }

  @Test
  void assignTraining_nonExistentTemplate_throwsIllegalArgumentException() {
    when(trainingTemplateRepository.findById(999L)).thenReturn(Optional.empty());

    AssignTrainingRequest request = new AssignTrainingRequest(List.of(2L), null);

    assertThrows(IllegalArgumentException.class,
        () -> trainingService.assignTraining(999L, request, testUser));
  }

  @Test
  void assignTraining_nonExistentUser_throwsIllegalArgumentException() {
    TrainingTemplate template = buildTemplate(1L, "Food Hygiene",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    when(trainingTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
    when(appUserRepository.findById(999L)).thenReturn(Optional.empty());

    AssignTrainingRequest request = new AssignTrainingRequest(List.of(999L), null);

    assertThrows(IllegalArgumentException.class,
        () -> trainingService.assignTraining(1L, request, testUser));
  }

  // --- completeAssignment tests ---

  @Test
  void completeAssignment_validInput_completesAndSetsExpiry() {
    TrainingTemplate template = buildTemplate(1L, "Food Hygiene",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    template.setAcknowledgmentRequired(true);
    template.setValidityDays(365);

    TrainingAssignment assignment = buildAssignment(10L, template, staffUser, testUser);
    when(trainingAssignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
    when(trainingCompletionRepository.save(any(TrainingCompletion.class)))
        .thenAnswer(invocation -> {
          TrainingCompletion saved = invocation.getArgument(0);
          saved.setId(50L);
          saved.setCompletedAt(LocalDateTime.now());
          return saved;
        });
    when(trainingAssignmentRepository.save(any(TrainingAssignment.class)))
        .thenAnswer(i -> i.getArgument(0));

    CompleteTrainingRequest request = new CompleteTrainingRequest(true, "All understood");

    TrainingCompletionResponse response =
        trainingService.completeAssignment(10L, request, staffUser);

    assertNotNull(response);
    assertEquals(50L, response.id());
    assertEquals("staff", response.completedByUsername());
    assertTrue(response.acknowledgementChecked());
    assertEquals("All understood", response.comments());
    assertNotNull(response.expiresAt());
    assertEquals(TrainingAssignmentStatus.COMPLETED, assignment.getStatus());
  }

  @Test
  void completeAssignment_acknowledgmentRequiredButNotChecked_throwsIllegalArgumentException() {
    TrainingTemplate template = buildTemplate(1L, "Food Hygiene",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    template.setAcknowledgmentRequired(true);

    TrainingAssignment assignment = buildAssignment(10L, template, staffUser, testUser);
    when(trainingAssignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

    CompleteTrainingRequest request = new CompleteTrainingRequest(false, null);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> trainingService.completeAssignment(10L, request, staffUser));
    assertTrue(ex.getMessage().contains("Acknowledgment is required"));
  }

  @Test
  void completeAssignment_noValidityDays_expiresAtIsNull() {
    TrainingTemplate template = buildTemplate(1L, "Custom Training",
        ModuleType.SHARED, TrainingCategory.CUSTOM);
    template.setAcknowledgmentRequired(false);
    template.setValidityDays(null);

    TrainingAssignment assignment = buildAssignment(10L, template, staffUser, testUser);
    when(trainingAssignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
    when(trainingCompletionRepository.save(any(TrainingCompletion.class)))
        .thenAnswer(invocation -> {
          TrainingCompletion saved = invocation.getArgument(0);
          saved.setId(51L);
          saved.setCompletedAt(LocalDateTime.now());
          return saved;
        });
    when(trainingAssignmentRepository.save(any(TrainingAssignment.class)))
        .thenAnswer(i -> i.getArgument(0));

    CompleteTrainingRequest request = new CompleteTrainingRequest(false, null);

    TrainingCompletionResponse response =
        trainingService.completeAssignment(10L, request, staffUser);

    assertNotNull(response);
    assertNull(response.expiresAt());
  }

  @Test
  void completeAssignment_nonExistentAssignment_throwsIllegalArgumentException() {
    when(trainingAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

    CompleteTrainingRequest request = new CompleteTrainingRequest(true, null);

    assertThrows(IllegalArgumentException.class,
        () -> trainingService.completeAssignment(999L, request, staffUser));
  }

  // --- getReport tests ---

  @Test
  void getReport_returnsCorrectStatistics() {
    TrainingTemplate t1 = buildTemplate(1L, "T1",
        ModuleType.IK_MAT, TrainingCategory.FOOD_HYGIENE);
    TrainingTemplate t2 = buildTemplate(2L, "T2",
        ModuleType.IK_ALKOHOL, TrainingCategory.AGE_CONTROL);

    TrainingAssignment a1 = buildAssignment(1L, t1, staffUser, testUser);
    a1.setStatus(TrainingAssignmentStatus.COMPLETED);
    TrainingAssignment a2 = buildAssignment(2L, t2, staffUser, testUser);
    a2.setStatus(TrainingAssignmentStatus.ASSIGNED);
    TrainingAssignment a3 = buildAssignment(3L, t1, testUser, testUser);
    a3.setStatus(TrainingAssignmentStatus.OVERDUE);

    when(trainingTemplateRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(t1, t2));
    when(trainingAssignmentRepository
        .findByTrainingTemplateOrganizationIdOrderByAssignedAtDesc(1L))
        .thenReturn(List.of(a1, a2, a3));
    when(trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(1L, TrainingAssignmentStatus.COMPLETED))
        .thenReturn(1L);
    when(trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(1L, TrainingAssignmentStatus.OVERDUE))
        .thenReturn(1L);

    TrainingReportResponse report = trainingService.getReport(1L);

    assertEquals(2, report.totalTemplates());
    assertEquals(3, report.totalAssignments());
    assertEquals(1, report.completedCount());
    assertEquals(1, report.overdueCount());
    assertEquals(100.0 / 3.0, report.completionRate(), 0.01);
  }

  @Test
  void getReport_noAssignments_returnsZeroCompletionRate() {
    when(trainingTemplateRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of());
    when(trainingAssignmentRepository
        .findByTrainingTemplateOrganizationIdOrderByAssignedAtDesc(1L))
        .thenReturn(List.of());
    when(trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(1L, TrainingAssignmentStatus.COMPLETED))
        .thenReturn(0L);
    when(trainingAssignmentRepository
        .countByTrainingTemplateOrganizationIdAndStatus(1L, TrainingAssignmentStatus.OVERDUE))
        .thenReturn(0L);

    TrainingReportResponse report = trainingService.getReport(1L);

    assertEquals(0, report.totalTemplates());
    assertEquals(0, report.totalAssignments());
    assertEquals(0.0, report.completionRate());
  }
}
