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
import stud.ntnu.no.fullstack_project.dto.deviation.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.notifications.NotificationType;
import stud.ntnu.no.fullstack_project.entity.operations.*;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.auth.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.operations.DeviationRepository;
import stud.ntnu.no.fullstack_project.service.operations.DeviationService;
import stud.ntnu.no.fullstack_project.service.operations.NotificationService;

@ExtendWith(MockitoExtension.class)
class DeviationServiceTest {

  @Mock
  private DeviationRepository deviationRepository;

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private DeviationService deviationService;

  private AppUser testUser;
  private AppUser assigneeUser;
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

    assigneeUser = new AppUser();
    assigneeUser.setId(2L);
    assigneeUser.setUsername("assignee");
    assigneeUser.setPassword("encoded");
    assigneeUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private Deviation buildDeviation(Long id, String title, DeviationStatus status) {
    Deviation deviation = new Deviation();
    deviation.setId(id);
    deviation.setOrganization(testOrg);
    deviation.setTitle(title);
    deviation.setDescription("Description for " + title);
    deviation.setCategory(ComplianceCategory.FOOD);
    deviation.setSeverity(DeviationSeverity.MEDIUM);
    deviation.setStatus(status);
    deviation.setReportedBy(testUser);
    deviation.setCreatedAt(LocalDateTime.now());
    deviation.setUpdatedAt(LocalDateTime.now());
    return deviation;
  }

  // --- createDeviation tests ---

  @Test
  void createDeviation_validInput_createsDeviationWithOpenStatus() {
    CreateDeviationRequest request = new CreateDeviationRequest(
        "Expired Food Found", "Found expired milk in fridge", "FOOD", "HIGH"
    );

    when(deviationRepository.save(any(Deviation.class))).thenAnswer(invocation -> {
      Deviation saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });

    DeviationResponse response = deviationService.createDeviation(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("Expired Food Found", response.title());
    assertEquals("FOOD", response.category());
    assertEquals("HIGH", response.severity());
    assertEquals("OPEN", response.status());
    assertEquals("reporter", response.reportedByUsername());
    assertNull(response.assignedToUsername());
    assertNull(response.resolvedByUsername());
  }

  @Test
  void createDeviation_invalidCategory_throwsIllegalArgumentException() {
    CreateDeviationRequest request = new CreateDeviationRequest(
        "Test", "Desc", "INVALID_CAT", "HIGH"
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> deviationService.createDeviation(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid category"));
  }

  @Test
  void createDeviation_invalidSeverity_throwsIllegalArgumentException() {
    CreateDeviationRequest request = new CreateDeviationRequest(
        "Test", "Desc", "FOOD", "INVALID_SEV"
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> deviationService.createDeviation(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid severity"));
  }

  // --- getDeviation tests ---

  @Test
  void getDeviation_returnsResponseWithComments() {
    Deviation deviation = buildDeviation(5L, "Test Deviation", DeviationStatus.OPEN);

    DeviationComment comment = new DeviationComment();
    comment.setId(1L);
    comment.setDeviation(deviation);
    comment.setAuthor(testUser);
    comment.setContent("This needs attention");
    comment.setCreatedAt(LocalDateTime.now());
    deviation.getComments().add(comment);

    when(deviationRepository.findById(5L)).thenReturn(Optional.of(deviation));

    DeviationResponse response = deviationService.getDeviation(5L);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals("Test Deviation", response.title());
    assertEquals(1, response.comments().size());
    assertEquals("This needs attention", response.comments().get(0).content());
    assertEquals("reporter", response.comments().get(0).authorUsername());
  }

  @Test
  void getDeviation_nonExistentId_throwsIllegalArgumentException() {
    when(deviationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> deviationService.getDeviation(999L));
  }

  // --- listDeviations tests ---

  @Test
  void listDeviations_noFilter_returnsAllForOrg() {
    Deviation d1 = buildDeviation(1L, "Dev 1", DeviationStatus.OPEN);
    Deviation d2 = buildDeviation(2L, "Dev 2", DeviationStatus.RESOLVED);
    d2.setResolvedBy(testUser);
    d2.setResolvedAt(LocalDateTime.now());

    when(deviationRepository.findByOrganizationIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(d1, d2));

    List<DeviationResponse> result = deviationService.listDeviations(1L, null, null);

    assertEquals(2, result.size());
    verify(deviationRepository).findByOrganizationIdOrderByCreatedAtDesc(1L);
  }

  @Test
  void listDeviations_statusFilter_works() {
    Deviation d1 = buildDeviation(1L, "Open Dev", DeviationStatus.OPEN);
    when(deviationRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(
        1L, DeviationStatus.OPEN))
        .thenReturn(List.of(d1));

    List<DeviationResponse> result = deviationService.listDeviations(1L, "OPEN", null);

    assertEquals(1, result.size());
    assertEquals("OPEN", result.get(0).status());
  }

  @Test
  void listDeviations_categoryFilter_works() {
    Deviation d1 = buildDeviation(1L, "Food Dev", DeviationStatus.OPEN);
    when(deviationRepository.findByOrganizationIdAndCategoryOrderByCreatedAtDesc(
        1L, ComplianceCategory.FOOD))
        .thenReturn(List.of(d1));

    List<DeviationResponse> result = deviationService.listDeviations(1L, null, "FOOD");

    assertEquals(1, result.size());
    assertEquals("FOOD", result.get(0).category());
  }

  @Test
  void listDeviations_invalidStatus_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> deviationService.listDeviations(1L, "INVALID_STATUS", null));
  }

  // --- updateDeviation tests ---

  @Test
  void updateDeviation_statusChangeToResolved_setsResolvedByAndResolvedAt() {
    Deviation deviation = buildDeviation(1L, "Dev to resolve", DeviationStatus.OPEN);
    when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
    when(deviationRepository.save(any(Deviation.class))).thenAnswer(i -> i.getArgument(0));

    UpdateDeviationRequest request = new UpdateDeviationRequest("RESOLVED", null);

    DeviationResponse response = deviationService.updateDeviation(1L, request, testUser);

    assertEquals("RESOLVED", response.status());
    assertEquals("reporter", response.resolvedByUsername());
    assertNotNull(response.resolvedAt());
    verify(deviationRepository).save(any(Deviation.class));
  }

  @Test
  void updateDeviation_statusChangeToInProgress_doesNotSetResolvedFields() {
    Deviation deviation = buildDeviation(1L, "Dev to progress", DeviationStatus.OPEN);
    when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
    when(deviationRepository.save(any(Deviation.class))).thenAnswer(i -> i.getArgument(0));

    UpdateDeviationRequest request = new UpdateDeviationRequest("IN_PROGRESS", null);

    DeviationResponse response = deviationService.updateDeviation(1L, request, testUser);

    assertEquals("IN_PROGRESS", response.status());
    assertNull(response.resolvedByUsername());
    assertNull(response.resolvedAt());
  }

  @Test
  void updateDeviation_assignment_createsNotification() {
    Deviation deviation = buildDeviation(1L, "Dev to assign", DeviationStatus.OPEN);
    when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
    when(appUserRepository.findById(2L)).thenReturn(Optional.of(assigneeUser));
    when(deviationRepository.save(any(Deviation.class))).thenAnswer(i -> i.getArgument(0));

    UpdateDeviationRequest request = new UpdateDeviationRequest(null, 2L);

    DeviationResponse response = deviationService.updateDeviation(1L, request, testUser);

    assertEquals("assignee", response.assignedToUsername());
    verify(notificationService).createNotification(
        eq(assigneeUser),
        eq("Deviation Assigned"),
        contains("Dev to assign"),
        eq(NotificationType.DEVIATION_ASSIGNED),
        eq(1L),
        eq("DEVIATION")
    );
  }

  @Test
  void updateDeviation_nonExistentId_throwsIllegalArgumentException() {
    when(deviationRepository.findById(999L)).thenReturn(Optional.empty());

    UpdateDeviationRequest request = new UpdateDeviationRequest("RESOLVED", null);
    assertThrows(IllegalArgumentException.class,
        () -> deviationService.updateDeviation(999L, request, testUser));
  }

  @Test
  void updateDeviation_invalidStatus_throwsIllegalArgumentException() {
    Deviation deviation = buildDeviation(1L, "Dev", DeviationStatus.OPEN);
    when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

    UpdateDeviationRequest request = new UpdateDeviationRequest("BANANA", null);

    assertThrows(IllegalArgumentException.class,
        () -> deviationService.updateDeviation(1L, request, testUser));
  }

  // --- addComment tests ---

  @Test
  void addComment_addsCommentToDeviation() {
    Deviation deviation = buildDeviation(1L, "Dev with comment", DeviationStatus.OPEN);
    when(deviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
    when(deviationRepository.save(any(Deviation.class))).thenAnswer(i -> i.getArgument(0));

    AddDeviationCommentRequest request = new AddDeviationCommentRequest("Need to investigate further");

    DeviationCommentResponse response = deviationService.addComment(1L, request, testUser);

    assertNotNull(response);
    assertEquals("Need to investigate further", response.content());
    assertEquals("reporter", response.authorUsername());
    assertNotNull(response.createdAt());
    assertEquals(1, deviation.getComments().size());
    verify(deviationRepository).save(deviation);
  }

  @Test
  void addComment_nonExistentDeviation_throwsIllegalArgumentException() {
    when(deviationRepository.findById(999L)).thenReturn(Optional.empty());

    AddDeviationCommentRequest request = new AddDeviationCommentRequest("Comment");

    assertThrows(IllegalArgumentException.class,
        () -> deviationService.addComment(999L, request, testUser));
  }
}
