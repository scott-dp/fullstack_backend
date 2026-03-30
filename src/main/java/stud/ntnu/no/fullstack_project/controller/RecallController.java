package stud.ntnu.no.fullstack_project.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.recall.CreateRecallRequest;
import stud.ntnu.no.fullstack_project.dto.recall.RecallCaseResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.RecallService;

/**
 * REST controller for recall case management.
 *
 * <p>Provides endpoints to create, list, and retrieve product recall cases
 * within the authenticated user's organization.</p>
 */
@RestController
@RequestMapping("/api/recalls")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recalls", description = "Endpoints for product recall case management")
public class RecallController {

  private final RecallService recallService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a new recall case",
      description = "Creates a new product recall case. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Recall case created successfully",
          content = @Content(schema = @Schema(implementation = RecallCaseResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or supplier not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<RecallCaseResponse> createRecall(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Recall case details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateRecallRequest.class),
              examples = @ExampleObject(name = "Create recall", value = """
                  {
                    "title": "Contaminated salmon batch",
                    "supplierId": 1,
                    "productName": "Atlantic Salmon Fillet",
                    "batchLot": "LOT-2025-0042",
                    "description": "Potential listeria contamination detected in batch."
                  }
                  """)))
      @Valid @RequestBody CreateRecallRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating recall case title={} by user={}", request.title(),
        currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(recallService.createRecall(request, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a recall case by ID",
      description = "Returns a single recall case."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Recall case found",
          content = @Content(schema = @Schema(implementation = RecallCaseResponse.class))),
      @ApiResponse(responseCode = "400", description = "Recall case not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<RecallCaseResponse> getRecall(@PathVariable Long id) {
    log.info("Fetching recall case id={}", id);
    return ResponseEntity.ok(recallService.getRecall(id));
  }

  @GetMapping
  @Operation(
      summary = "List recall cases for the current user's organization",
      description = "Returns all recall cases ordered by opened date descending."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Recall cases retrieved successfully")
  })
  public ResponseEntity<List<RecallCaseResponse>> listRecalls(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing recall cases for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        recallService.listRecalls(currentUser.getOrganization().getId()));
  }
}
