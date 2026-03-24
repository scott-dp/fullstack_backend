package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistCompletionResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.ChecklistTemplateResponse;
import stud.ntnu.no.fullstack_project.dto.checklist.CompleteChecklistRequest;
import stud.ntnu.no.fullstack_project.dto.checklist.CreateChecklistTemplateRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.ChecklistService;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklists", description = "Endpoints for checklist templates and completions")
public class ChecklistController {

  private final ChecklistService checklistService;

  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Create a new checklist template")
  public ResponseEntity<ChecklistTemplateResponse> createTemplate(
      @Valid @RequestBody CreateChecklistTemplateRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(checklistService.createTemplate(request, currentUser));
  }

  @GetMapping("/templates")
  @Operation(summary = "List checklist templates for the current user's organization")
  public ResponseEntity<List<ChecklistTemplateResponse>> listTemplates(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String category
  ) {
    return ResponseEntity.ok(
        checklistService.listTemplates(currentUser.getOrganization().getId(), category)
    );
  }

  @GetMapping("/templates/{id}")
  @Operation(summary = "Get a checklist template by ID")
  public ResponseEntity<ChecklistTemplateResponse> getTemplate(@PathVariable Long id) {
    return ResponseEntity.ok(checklistService.getTemplate(id));
  }

  @PutMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update a checklist template")
  public ResponseEntity<ChecklistTemplateResponse> updateTemplate(
      @PathVariable Long id,
      @Valid @RequestBody CreateChecklistTemplateRequest request
  ) {
    return ResponseEntity.ok(checklistService.updateTemplate(id, request));
  }

  @DeleteMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Soft-delete a checklist template")
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
    checklistService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/completions")
  @Operation(summary = "Complete a checklist")
  public ResponseEntity<ChecklistCompletionResponse> completeChecklist(
      @Valid @RequestBody CompleteChecklistRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(checklistService.completeChecklist(request, currentUser));
  }

  @GetMapping("/completions")
  @Operation(summary = "List checklist completions for the current user's organization")
  public ResponseEntity<List<ChecklistCompletionResponse>> listCompletions(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(
        checklistService.listCompletions(currentUser.getOrganization().getId())
    );
  }

  @GetMapping("/completions/{id}")
  @Operation(summary = "Get a checklist completion by ID")
  public ResponseEntity<ChecklistCompletionResponse> getCompletion(@PathVariable Long id) {
    return ResponseEntity.ok(checklistService.getCompletion(id));
  }
}
