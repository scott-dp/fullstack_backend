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
import stud.ntnu.no.fullstack_project.dto.incident.AlcoholIncidentResponse;
import stud.ntnu.no.fullstack_project.dto.incident.CloseIncidentRequest;
import stud.ntnu.no.fullstack_project.dto.incident.CreateAlcoholIncidentRequest;
import stud.ntnu.no.fullstack_project.dto.incident.IncidentReportResponse;
import stud.ntnu.no.fullstack_project.dto.incident.UpdateAlcoholIncidentRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.AlcoholIncidentService;

/**
 * REST controller for alcohol incident reporting and management.
 *
 * <p>Provides endpoints to create, list, update, close, and report on
 * alcohol-related incidents within the authenticated user's organization.</p>
 */
@RestController
@RequestMapping("/api/alcohol-incidents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alcohol Incidents", description = "Endpoints for alcohol incident and refusal log management")
public class AlcoholIncidentController {

  private final AlcoholIncidentService alcoholIncidentService;

  @PostMapping
  @Operation(
      summary = "Create a new alcohol incident",
      description = "Reports a new alcohol-related incident for the current user's organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Incident created successfully",
          content = @Content(schema = @Schema(implementation = AlcoholIncidentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or invalid type/severity",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AlcoholIncidentResponse> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Alcohol incident details to report.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateAlcoholIncidentRequest.class),
              examples = @ExampleObject(name = "Create incident", value = """
                  {
                    "occurredAt": "2026-03-25T22:30:00",
                    "shiftLabel": "Evening Shift",
                    "locationArea": "Bar Area",
                    "incidentType": "AGE_DOUBT_REFUSAL",
                    "severity": "MEDIUM",
                    "description": "Customer could not provide valid ID.",
                    "immediateActionTaken": "Refused service.",
                    "followUpRequired": false
                  }
                  """)))
      @Valid @RequestBody CreateAlcoholIncidentRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating alcohol incident type={} by user={}", request.incidentType(),
        currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(alcoholIncidentService.create(request, currentUser));
  }

  @GetMapping
  @Operation(
      summary = "List alcohol incidents for the current user's organization",
      description = "Returns alcohol incidents optionally filtered by status or incident type."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid status or type filter",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<AlcoholIncidentResponse>> list(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type
  ) {
    log.info("Listing alcohol incidents for orgId={}, status={}, type={}",
        currentUser.getOrganization().getId(), status, type);
    return ResponseEntity.ok(
        alcoholIncidentService.list(
            currentUser.getOrganization().getId(), status, type
        )
    );
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get an alcohol incident by ID",
      description = "Returns a single alcohol incident record."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Incident found",
          content = @Content(schema = @Schema(implementation = AlcoholIncidentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Incident not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AlcoholIncidentResponse> get(@PathVariable Long id) {
    log.info("Fetching alcohol incident id={}", id);
    return ResponseEntity.ok(alcoholIncidentService.get(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update an alcohol incident",
      description = "Updates an alcohol incident's details. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Incident updated successfully",
          content = @Content(schema = @Schema(implementation = AlcoholIncidentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or incident not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AlcoholIncidentResponse> update(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Fields to update on the incident.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateAlcoholIncidentRequest.class),
              examples = @ExampleObject(name = "Update incident", value = """
                  {"status": "UNDER_REVIEW", "severity": "HIGH"}
                  """)))
      @Valid @RequestBody UpdateAlcoholIncidentRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Updating alcohol incident id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.ok(alcoholIncidentService.update(id, request, currentUser));
  }

  @PostMapping("/{id}/close")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Close an alcohol incident",
      description = "Closes an alcohol incident with optional notes. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Incident closed successfully",
          content = @Content(schema = @Schema(implementation = AlcoholIncidentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Incident not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<AlcoholIncidentResponse> close(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Optional closing notes.",
          required = true,
          content = @Content(schema = @Schema(implementation = CloseIncidentRequest.class),
              examples = @ExampleObject(name = "Close incident", value = """
                  {"notes": "Issue resolved, no further action needed."}
                  """)))
      @Valid @RequestBody CloseIncidentRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Closing alcohol incident id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.ok(alcoholIncidentService.close(id, request, currentUser));
  }

  @GetMapping("/report")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Get alcohol incident report",
      description = "Returns a summary report of alcohol incidents. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report generated successfully",
          content = @Content(schema = @Schema(implementation = IncidentReportResponse.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<IncidentReportResponse> report(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Generating alcohol incident report for orgId={}",
        currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        alcoholIncidentService.report(currentUser.getOrganization().getId())
    );
  }
}
