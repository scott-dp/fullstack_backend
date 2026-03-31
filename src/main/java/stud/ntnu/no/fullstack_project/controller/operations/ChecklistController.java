package stud.ntnu.no.fullstack_project.controller.operations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.ChecklistService;

/**
 * REST controller for checklist template and completion management.
 *
 * <p>Provides CRUD operations for checklist templates and endpoints for
 * completing checklists and reviewing past completions.</p>
 */
@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Checklists", description = "Endpoints for checklist templates and completions")
public class ChecklistController {

  private final ChecklistService checklistService;

  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a new checklist template",
      description = "Creates a new checklist template with its items. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Template created successfully",
          content = @Content(schema = @Schema(implementation = ChecklistTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ChecklistTemplateResponse> createTemplate(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Checklist template details including items.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateChecklistTemplateRequest.class),
              examples = @ExampleObject(name = "Create template", value = """
                  {
                    "title": "Morning Kitchen Checklist",
                    "description": "Daily morning kitchen opening procedures.",
                    "frequency": "DAILY",
                    "category": "FOOD_SAFETY",
                    "items": [
                      {"description": "Verify fridge temperature is below 4C", "sortOrder": 1, "requiresComment": true},
                      {"description": "Check hand-washing stations", "sortOrder": 2, "requiresComment": false}
                    ]
                  }
                  """)))
      @Valid @RequestBody CreateChecklistTemplateRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating checklist template title={} by user={}", request.title(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(checklistService.createTemplate(request, currentUser));
  }

  @GetMapping("/templates")
  @Operation(
      summary = "List checklist templates for the current user's organization",
      description = "Returns all active checklist templates, optionally filtered by compliance category."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
  })
  public ResponseEntity<List<ChecklistTemplateResponse>> listTemplates(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String category
  ) {
    log.info("Listing checklist templates for orgId={}, category={}", currentUser.getOrganization().getId(), category);
    return ResponseEntity.ok(
        checklistService.listTemplates(currentUser.getOrganization().getId(), category)
    );
  }

  @GetMapping("/templates/{id}")
  @Operation(
      summary = "Get a checklist template by ID",
      description = "Returns a single checklist template with all its items."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Template found",
          content = @Content(schema = @Schema(implementation = ChecklistTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Template not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ChecklistTemplateResponse> getTemplate(@PathVariable Long id) {
    log.info("Fetching checklist template id={}", id);
    return ResponseEntity.ok(checklistService.getTemplate(id));
  }

  @PutMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update a checklist template",
      description = "Replaces the template details and items. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Template updated successfully",
          content = @Content(schema = @Schema(implementation = ChecklistTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or template not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ChecklistTemplateResponse> updateTemplate(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Updated checklist template details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateChecklistTemplateRequest.class)))
      @Valid @RequestBody CreateChecklistTemplateRequest request
  ) {
    log.info("Updating checklist template id={}", id);
    return ResponseEntity.ok(checklistService.updateTemplate(id, request));
  }

  @DeleteMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Soft-delete a checklist template",
      description = "Marks the template as inactive. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Template soft-deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Template not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
    log.info("Soft-deleting checklist template id={}", id);
    checklistService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/completions")
  @Operation(
      summary = "Complete a checklist",
      description = "Submits answers for a checklist template and records the completion."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Checklist completed successfully",
          content = @Content(schema = @Schema(implementation = ChecklistCompletionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or template/item not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ChecklistCompletionResponse> completeChecklist(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Checklist completion with answers for each item.",
          required = true,
          content = @Content(schema = @Schema(implementation = CompleteChecklistRequest.class),
              examples = @ExampleObject(name = "Complete checklist", value = """
                  {
                    "templateId": 1,
                    "answers": [
                      {"itemId": 1, "checked": true, "comment": "Temperature was 3.5C"},
                      {"itemId": 2, "checked": true, "comment": null}
                    ],
                    "comment": "All items checked during morning shift."
                  }
                  """)))
      @Valid @RequestBody CompleteChecklistRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Completing checklist templateId={} by user={}", request.templateId(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(checklistService.completeChecklist(request, currentUser));
  }

  @GetMapping("/completions")
  @Operation(
      summary = "List checklist completions for the current user's organization",
      description = "Returns all checklist completions ordered by most recent first."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Completions retrieved successfully")
  })
  public ResponseEntity<List<ChecklistCompletionResponse>> listCompletions(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing checklist completions for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        checklistService.listCompletions(currentUser.getOrganization().getId())
    );
  }

  @GetMapping("/completions/{id}")
  @Operation(
      summary = "Get a checklist completion by ID",
      description = "Returns a single checklist completion with all its answers."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Completion found",
          content = @Content(schema = @Schema(implementation = ChecklistCompletionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Completion not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ChecklistCompletionResponse> getCompletion(@PathVariable Long id) {
    log.info("Fetching checklist completion id={}", id);
    return ResponseEntity.ok(checklistService.getCompletion(id));
  }
}
