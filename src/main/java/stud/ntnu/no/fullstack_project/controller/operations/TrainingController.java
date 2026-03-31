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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.training.AssignTrainingRequest;
import stud.ntnu.no.fullstack_project.dto.training.CompleteTrainingRequest;
import stud.ntnu.no.fullstack_project.dto.training.CreateTrainingTemplateRequest;
import stud.ntnu.no.fullstack_project.dto.training.TrainingAssignmentResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingCompletionResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingReportResponse;
import stud.ntnu.no.fullstack_project.dto.training.TrainingTemplateResponse;
import stud.ntnu.no.fullstack_project.dto.training.UpdateTrainingTemplateRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.TrainingService;

/**
 * REST controller for training template management, assignment, and completion.
 *
 * <p>Provides endpoints to create, list, update, assign, and complete training
 * within the authenticated user's organization.</p>
 */
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainings", description = "Endpoints for training management, assignment, and completion")
public class TrainingController {

  private final TrainingService trainingService;

  @GetMapping("/templates")
  @Operation(
      summary = "List training templates",
      description = "Returns all training templates for the current user's organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
  })
  public ResponseEntity<List<TrainingTemplateResponse>> listTemplates(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing training templates for orgId={}",
        currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        trainingService.listTemplates(currentUser.getOrganization().getId()));
  }

  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a training template",
      description = "Creates a new training template. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Template created successfully",
          content = @Content(schema = @Schema(implementation = TrainingTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or invalid enum value",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TrainingTemplateResponse> createTemplate(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Training template details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateTrainingTemplateRequest.class),
              examples = @ExampleObject(name = "Create template", value = """
                  {
                    "title": "Basic food hygiene",
                    "moduleType": "IK_MAT",
                    "category": "FOOD_HYGIENE",
                    "description": "Covers basic food hygiene principles.",
                    "requiredForRole": "ALL",
                    "isMandatory": true,
                    "validityDays": 365,
                    "acknowledgmentRequired": true
                  }
                  """)))
      @Valid @RequestBody CreateTrainingTemplateRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating training template title={} by user={}",
        request.title(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(trainingService.createTemplate(request, currentUser));
  }

  @GetMapping("/templates/{id}")
  @Operation(
      summary = "Get a training template by ID",
      description = "Returns a single training template by its identifier."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Template found",
          content = @Content(schema = @Schema(implementation = TrainingTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Template not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TrainingTemplateResponse> getTemplate(@PathVariable Long id) {
    log.info("Fetching training template id={}", id);
    return ResponseEntity.ok(trainingService.getTemplate(id));
  }

  @PutMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update a training template",
      description = "Updates an existing training template. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Template updated successfully",
          content = @Content(schema = @Schema(implementation = TrainingTemplateResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or template not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TrainingTemplateResponse> updateTemplate(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Fields to update on the template.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateTrainingTemplateRequest.class),
              examples = @ExampleObject(name = "Update template", value = """
                  {"title": "Advanced food hygiene", "validityDays": 180}
                  """)))
      @Valid @RequestBody UpdateTrainingTemplateRequest request
  ) {
    log.info("Updating training template id={}", id);
    return ResponseEntity.ok(trainingService.updateTemplate(id, request));
  }

  @DeleteMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Delete a training template",
      description = "Deletes a training template and all of its assignments and completions."
  )
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
    log.info("Deleting training template id={}", id);
    trainingService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/templates/{id}/assign")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Assign a training template to users",
      description = "Assigns a training template to one or more users. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Training assigned successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed or template/user not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<TrainingAssignmentResponse>> assignTraining(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Assignment details.",
          required = true,
          content = @Content(schema = @Schema(implementation = AssignTrainingRequest.class),
              examples = @ExampleObject(name = "Assign training", value = """
                  {"assigneeUserIds": [2, 3], "dueAt": "2025-06-01T00:00:00"}
                  """)))
      @Valid @RequestBody AssignTrainingRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Assigning training templateId={} to {} users by user={}",
        id, request.assigneeUserIds().size(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(trainingService.assignTraining(id, request, currentUser));
  }

  @GetMapping("/assignments/my")
  @Operation(
      summary = "Get current user's training assignments",
      description = "Returns all training assignments for the authenticated user."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully")
  })
  public ResponseEntity<List<TrainingAssignmentResponse>> getMyAssignments(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Fetching training assignments for user={}", currentUser.getUsername());
    return ResponseEntity.ok(trainingService.getMyAssignments(currentUser));
  }

  @PostMapping("/assignments/{id}/complete")
  @Operation(
      summary = "Complete a training assignment",
      description = "Marks a training assignment as completed by the authenticated user."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Training completed successfully",
          content = @Content(schema = @Schema(implementation = TrainingCompletionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or assignment not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TrainingCompletionResponse> completeAssignment(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Completion details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CompleteTrainingRequest.class),
              examples = @ExampleObject(name = "Complete training", value = """
                  {"acknowledgementChecked": true, "comments": "Understood all material."}
                  """)))
      @Valid @RequestBody CompleteTrainingRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Completing training assignmentId={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(trainingService.completeAssignment(id, request, currentUser));
  }

  @GetMapping("/report")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Get training report",
      description = "Returns training statistics for the current user's organization. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report generated successfully",
          content = @Content(schema = @Schema(implementation = TrainingReportResponse.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TrainingReportResponse> getReport(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Generating training report for orgId={}",
        currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        trainingService.getReport(currentUser.getOrganization().getId()));
  }
}
