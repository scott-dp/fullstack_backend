package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.checklist.*;
import stud.ntnu.no.fullstack_project.entity.*;
import stud.ntnu.no.fullstack_project.repository.ChecklistCompletionRepository;
import stud.ntnu.no.fullstack_project.repository.ChecklistTemplateRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

  @Mock
  private ChecklistTemplateRepository templateRepository;

  @Mock
  private ChecklistCompletionRepository completionRepository;

  @InjectMocks
  private ChecklistService checklistService;

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

  // --- Helper methods ---

  private ChecklistTemplate buildTemplate(Long id, String title) {
    ChecklistTemplate template = new ChecklistTemplate();
    template.setId(id);
    template.setTitle(title);
    template.setDescription("Description for " + title);
    template.setFrequency(Frequency.DAILY);
    template.setCategory(ComplianceCategory.FOOD);
    template.setOrganization(testOrg);
    template.setCreatedBy(testUser);
    template.setActive(true);
    template.setCreatedAt(LocalDateTime.now());
    return template;
  }

  private ChecklistItem buildItem(Long id, ChecklistTemplate template, String desc, int order) {
    ChecklistItem item = new ChecklistItem();
    item.setId(id);
    item.setTemplate(template);
    item.setDescription(desc);
    item.setSortOrder(order);
    item.setRequiresComment(false);
    return item;
  }

  private CreateChecklistTemplateRequest buildCreateRequest() {
    List<CreateChecklistItemRequest> items = List.of(
        new CreateChecklistItemRequest("Check fridge temp", 1, false),
        new CreateChecklistItemRequest("Check freezer temp", 2, true)
    );
    return new CreateChecklistTemplateRequest(
        "Daily Kitchen Checklist", "Morning checks", "DAILY", "FOOD", items
    );
  }

  // --- createTemplate tests ---

  @Test
  void createTemplate_validInput_createsTemplateAndReturnsResponse() {
    CreateChecklistTemplateRequest request = buildCreateRequest();

    when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(invocation -> {
      ChecklistTemplate saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      return saved;
    });

    ChecklistTemplateResponse response = checklistService.createTemplate(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("Daily Kitchen Checklist", response.title());
    assertEquals("Morning checks", response.description());
    assertEquals("DAILY", response.frequency());
    assertEquals("FOOD", response.category());
    assertTrue(response.active());
    assertEquals("testuser", response.createdByUsername());
    assertEquals(2, response.items().size());
    verify(templateRepository).save(any(ChecklistTemplate.class));
  }

  @Test
  void createTemplate_invalidFrequency_throwsIllegalArgumentException() {
    List<CreateChecklistItemRequest> items = List.of(
        new CreateChecklistItemRequest("Item 1", 1, false)
    );
    CreateChecklistTemplateRequest request = new CreateChecklistTemplateRequest(
        "Test", "Desc", "INVALID_FREQUENCY", "FOOD", items
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> checklistService.createTemplate(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid frequency"));
  }

  @Test
  void createTemplate_invalidCategory_throwsIllegalArgumentException() {
    List<CreateChecklistItemRequest> items = List.of(
        new CreateChecklistItemRequest("Item 1", 1, false)
    );
    CreateChecklistTemplateRequest request = new CreateChecklistTemplateRequest(
        "Test", "Desc", "DAILY", "INVALID_CATEGORY", items
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> checklistService.createTemplate(request, testUser));
    assertTrue(ex.getMessage().contains("Invalid category"));
  }

  // --- getTemplate tests ---

  @Test
  void getTemplate_existingTemplate_returnsResponse() {
    ChecklistTemplate template = buildTemplate(5L, "Morning Checklist");
    ChecklistItem item = buildItem(1L, template, "Check temperatures", 1);
    template.getItems().add(item);

    when(templateRepository.findById(5L)).thenReturn(Optional.of(template));

    ChecklistTemplateResponse response = checklistService.getTemplate(5L);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals("Morning Checklist", response.title());
    assertEquals(1, response.items().size());
    assertEquals("Check temperatures", response.items().get(0).description());
  }

  @Test
  void getTemplate_nonExistentId_throwsIllegalArgumentException() {
    when(templateRepository.findById(999L)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> checklistService.getTemplate(999L));
    assertTrue(ex.getMessage().contains("not found"));
  }

  // --- listTemplates tests ---

  @Test
  void listTemplates_filtersByCategory_whenCategoryProvided() {
    ChecklistTemplate template = buildTemplate(1L, "Food Checklist");
    when(templateRepository.findByOrganizationIdAndCategoryAndActiveTrue(1L, ComplianceCategory.FOOD))
        .thenReturn(List.of(template));

    List<ChecklistTemplateResponse> result = checklistService.listTemplates(1L, "FOOD");

    assertEquals(1, result.size());
    assertEquals("Food Checklist", result.get(0).title());
    verify(templateRepository).findByOrganizationIdAndCategoryAndActiveTrue(1L, ComplianceCategory.FOOD);
    verify(templateRepository, never()).findByOrganizationIdAndActiveTrue(anyLong());
  }

  @Test
  void listTemplates_returnsAllActive_whenNoCategoryFilter() {
    ChecklistTemplate t1 = buildTemplate(1L, "Template 1");
    ChecklistTemplate t2 = buildTemplate(2L, "Template 2");
    when(templateRepository.findByOrganizationIdAndActiveTrue(1L))
        .thenReturn(List.of(t1, t2));

    List<ChecklistTemplateResponse> result = checklistService.listTemplates(1L, null);

    assertEquals(2, result.size());
    verify(templateRepository).findByOrganizationIdAndActiveTrue(1L);
  }

  @Test
  void listTemplates_returnsAllActive_whenCategoryIsBlank() {
    when(templateRepository.findByOrganizationIdAndActiveTrue(1L))
        .thenReturn(List.of());

    List<ChecklistTemplateResponse> result = checklistService.listTemplates(1L, "  ");

    assertEquals(0, result.size());
    verify(templateRepository).findByOrganizationIdAndActiveTrue(1L);
  }

  // --- deleteTemplate tests ---

  @Test
  void deleteTemplate_setsActiveToFalse() {
    ChecklistTemplate template = buildTemplate(3L, "To Delete");
    assertTrue(template.isActive());
    when(templateRepository.findById(3L)).thenReturn(Optional.of(template));
    when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(i -> i.getArgument(0));

    checklistService.deleteTemplate(3L);

    assertFalse(template.isActive());
    verify(templateRepository).save(template);
  }

  @Test
  void deleteTemplate_nonExistentId_throwsIllegalArgumentException() {
    when(templateRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> checklistService.deleteTemplate(999L));
  }

  // --- completeChecklist tests ---

  @Test
  void completeChecklist_allChecked_returnsCompleteStatus() {
    ChecklistTemplate template = buildTemplate(1L, "Checklist");
    ChecklistItem item1 = buildItem(10L, template, "Item A", 1);
    ChecklistItem item2 = buildItem(20L, template, "Item B", 2);
    template.getItems().add(item1);
    template.getItems().add(item2);

    when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
    when(completionRepository.save(any(ChecklistCompletion.class))).thenAnswer(invocation -> {
      ChecklistCompletion saved = invocation.getArgument(0);
      saved.setId(100L);
      saved.setCompletedAt(LocalDateTime.now());
      return saved;
    });

    CompleteChecklistRequest request = new CompleteChecklistRequest(1L, List.of(
        new ChecklistAnswerRequest(10L, true, null),
        new ChecklistAnswerRequest(20L, true, "Looks good")
    ), "All done");

    ChecklistCompletionResponse response = checklistService.completeChecklist(request, testUser);

    assertNotNull(response);
    assertEquals("COMPLETE", response.status());
    assertEquals("All done", response.comment());
    assertEquals("testuser", response.completedByUsername());
    assertEquals(2, response.answers().size());
  }

  @Test
  void completeChecklist_someUnchecked_returnsIncompleteStatus() {
    ChecklistTemplate template = buildTemplate(1L, "Checklist");
    ChecklistItem item1 = buildItem(10L, template, "Item A", 1);
    ChecklistItem item2 = buildItem(20L, template, "Item B", 2);
    template.getItems().add(item1);
    template.getItems().add(item2);

    when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
    when(completionRepository.save(any(ChecklistCompletion.class))).thenAnswer(invocation -> {
      ChecklistCompletion saved = invocation.getArgument(0);
      saved.setId(101L);
      saved.setCompletedAt(LocalDateTime.now());
      return saved;
    });

    CompleteChecklistRequest request = new CompleteChecklistRequest(1L, List.of(
        new ChecklistAnswerRequest(10L, true, null),
        new ChecklistAnswerRequest(20L, false, "Not done yet")
    ), null);

    ChecklistCompletionResponse response = checklistService.completeChecklist(request, testUser);

    assertEquals("INCOMPLETE", response.status());
  }

  @Test
  void completeChecklist_invalidTemplateId_throwsIllegalArgumentException() {
    when(templateRepository.findById(999L)).thenReturn(Optional.empty());

    CompleteChecklistRequest request = new CompleteChecklistRequest(999L, List.of(
        new ChecklistAnswerRequest(1L, true, null)
    ), null);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> checklistService.completeChecklist(request, testUser));
    assertTrue(ex.getMessage().contains("not found"));
  }

  @Test
  void completeChecklist_invalidItemId_throwsIllegalArgumentException() {
    ChecklistTemplate template = buildTemplate(1L, "Checklist");
    ChecklistItem item1 = buildItem(10L, template, "Item A", 1);
    template.getItems().add(item1);

    when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

    CompleteChecklistRequest request = new CompleteChecklistRequest(1L, List.of(
        new ChecklistAnswerRequest(999L, true, null)
    ), null);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> checklistService.completeChecklist(request, testUser));
    assertTrue(ex.getMessage().contains("Checklist item not found"));
  }
}
