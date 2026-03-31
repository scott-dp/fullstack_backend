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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.deviation.AddDeviationCommentRequest;
import stud.ntnu.no.fullstack_project.dto.deviation.CreateDeviationRequest;
import stud.ntnu.no.fullstack_project.dto.deviation.DeviationCommentResponse;
import stud.ntnu.no.fullstack_project.dto.deviation.DeviationResponse;
import stud.ntnu.no.fullstack_project.dto.deviation.UpdateDeviationRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.DeviationService;

/**
 * REST controller for deviation reporting and management.
 *
 * <p>Provides endpoints to create, list, update, and comment on compliance
 * deviations within the authenticated user's organization.</p>
 */
@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deviations", description = "Endpoints for deviation reporting and management")
public class DeviationController {

  private final DeviationService deviationService;

  @PostMapping
  @Operation(
      summary = "Create a new deviation",
      description = "Reports a new compliance deviation for the current user's organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Deviation created successfully",
          content = @Content(schema = @Schema(implementation = DeviationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or invalid category/severity",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeviationResponse> createDeviation(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Deviation details to report.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateDeviationRequest.class),
              examples = @ExampleObject(name = "Create deviation", value = """
                  {
                    "title": "Fridge temperature too high",
                    "description": "Walk-in fridge measured at 8C during morning check.",
                    "category": "FOOD_SAFETY",
                    "severity": "HIGH"
                  }
                  """)))
      @Valid @RequestBody CreateDeviationRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating deviation title={} by user={}", request.title(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deviationService.createDeviation(request, currentUser));
  }

  @GetMapping
  @Operation(
      summary = "List deviations for the current user's organization",
      description = "Returns deviations optionally filtered by status or compliance category."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deviations retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid status or category filter",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<DeviationResponse>> listDeviations(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category
  ) {
    log.info("Listing deviations for orgId={}, status={}, category={}",
        currentUser.getOrganization().getId(), status, category);
    return ResponseEntity.ok(
        deviationService.listDeviations(
            currentUser.getOrganization().getId(), status, category
        )
    );
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a deviation by ID with comments",
      description = "Returns a single deviation including all associated comments."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deviation found",
          content = @Content(schema = @Schema(implementation = DeviationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Deviation not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeviationResponse> getDeviation(@PathVariable Long id) {
    log.info("Fetching deviation id={}", id);
    return ResponseEntity.ok(deviationService.getDeviation(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update a deviation's status or assignment",
      description = "Updates the status and/or assignee of a deviation. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deviation updated successfully",
          content = @Content(schema = @Schema(implementation = DeviationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or deviation/user not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeviationResponse> updateDeviation(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Fields to update on the deviation.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateDeviationRequest.class),
              examples = @ExampleObject(name = "Update deviation", value = """
                  {"status": "IN_PROGRESS", "assignedToId": 2}
                  """)))
      @Valid @RequestBody UpdateDeviationRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Updating deviation id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.ok(deviationService.updateDeviation(id, request, currentUser));
  }

  @PostMapping("/{id}/comments")
  @Operation(
      summary = "Add a comment to a deviation",
      description = "Adds a text comment to an existing deviation."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Comment added successfully",
          content = @Content(schema = @Schema(implementation = DeviationCommentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or deviation not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeviationCommentResponse> addComment(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Comment to add to the deviation.",
          required = true,
          content = @Content(schema = @Schema(implementation = AddDeviationCommentRequest.class),
              examples = @ExampleObject(name = "Add comment", value = """
                  {"content": "Technician has been called to fix the fridge."}
                  """)))
      @Valid @RequestBody AddDeviationCommentRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Adding comment to deviation id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deviationService.addComment(id, request, currentUser));
  }
}
