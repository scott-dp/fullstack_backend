package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.bevilling.BevillingResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.ConditionResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.CreateBevillingRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.CreateConditionRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.ServingHoursEntry;
import stud.ntnu.no.fullstack_project.dto.bevilling.ServingHoursResponse;
import stud.ntnu.no.fullstack_project.dto.bevilling.UpdateBevillingRequest;
import stud.ntnu.no.fullstack_project.dto.bevilling.UpdateConditionRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.BevillingService;

/**
 * REST controller for bevilling (alcohol license) management.
 *
 * <p>Provides endpoints to create, list, update bevillinger, manage conditions,
 * and configure serving hours.</p>
 */
@RestController
@RequestMapping("/api/bevillinger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bevillinger", description = "Endpoints for bevilling (alcohol license) management")
public class BevillingController {

  private final BevillingService bevillingService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Create a new bevilling",
      description = "Creates a new alcohol license record for the organization.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Bevilling created successfully",
          content = @Content(schema = @Schema(implementation = BevillingResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<BevillingResponse> create(
      @Valid @RequestBody CreateBevillingRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating bevilling type={} by user={}", request.bevillingType(),
        currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bevillingService.create(request, currentUser));
  }

  @GetMapping
  @Operation(summary = "List all bevillinger for the organization")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Bevillinger retrieved successfully")
  })
  public ResponseEntity<List<BevillingResponse>> list(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing bevillinger for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        bevillingService.list(currentUser.getOrganization().getId())
    );
  }

  @GetMapping("/current")
  @Operation(summary = "Get the current active bevilling for the organization")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Active bevilling found or null"),
  })
  public ResponseEntity<BevillingResponse> getCurrent(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Getting current bevilling for orgId={}", currentUser.getOrganization().getId());
    BevillingResponse response = bevillingService.getCurrent(
        currentUser.getOrganization().getId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a bevilling by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Bevilling found",
          content = @Content(schema = @Schema(implementation = BevillingResponse.class))),
      @ApiResponse(responseCode = "400", description = "Bevilling not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<BevillingResponse> get(@PathVariable Long id) {
    log.info("Fetching bevilling id={}", id);
    return ResponseEntity.ok(bevillingService.get(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update a bevilling",
      description = "Updates bevilling details. Requires ADMIN or MANAGER role.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Bevilling updated successfully",
          content = @Content(schema = @Schema(implementation = BevillingResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<BevillingResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody UpdateBevillingRequest request
  ) {
    log.info("Updating bevilling id={}", id);
    return ResponseEntity.ok(bevillingService.update(id, request));
  }

  @PostMapping("/{id}/conditions")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Add a condition to a bevilling")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Condition added successfully",
          content = @Content(schema = @Schema(implementation = ConditionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ConditionResponse> addCondition(
      @PathVariable Long id,
      @Valid @RequestBody CreateConditionRequest request
  ) {
    log.info("Adding condition to bevilling id={}", id);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bevillingService.addCondition(id, request));
  }

  @PutMapping("/conditions/{conditionId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update a bevilling condition")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Condition updated successfully",
          content = @Content(schema = @Schema(implementation = ConditionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Condition not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<ConditionResponse> updateCondition(
      @PathVariable Long conditionId,
      @Valid @RequestBody UpdateConditionRequest request
  ) {
    log.info("Updating condition id={}", conditionId);
    return ResponseEntity.ok(bevillingService.updateCondition(conditionId, request));
  }

  @PutMapping("/{id}/serving-hours")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Set serving hours for a bevilling",
      description = "Replaces all serving hours with the provided entries.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Serving hours updated successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<ServingHoursResponse>> setServingHours(
      @PathVariable Long id,
      @Valid @RequestBody List<ServingHoursEntry> entries
  ) {
    log.info("Setting serving hours for bevilling id={}, entries={}", id, entries.size());
    return ResponseEntity.ok(bevillingService.setServingHours(id, entries));
  }

  @GetMapping("/{id}/serving-hours")
  @Operation(summary = "Get serving hours for a bevilling")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Serving hours retrieved successfully")
  })
  public ResponseEntity<List<ServingHoursResponse>> getServingHours(@PathVariable Long id) {
    log.info("Fetching serving hours for bevilling id={}", id);
    return ResponseEntity.ok(bevillingService.getServingHours(id));
  }
}
