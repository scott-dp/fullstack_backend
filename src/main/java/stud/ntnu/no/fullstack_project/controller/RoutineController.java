package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.routine.CreateRoutineRequest;
import stud.ntnu.no.fullstack_project.dto.routine.ReviewRoutineRequest;
import stud.ntnu.no.fullstack_project.dto.routine.RoutineResponse;
import stud.ntnu.no.fullstack_project.dto.routine.RoutineReviewResponse;
import stud.ntnu.no.fullstack_project.dto.routine.UpdateRoutineRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.RoutineService;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routines", description = "Endpoints for routine definition and review management")
public class RoutineController {

  private final RoutineService routineService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Create a new routine")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Routine created"),
      @ApiResponse(responseCode = "400", description = "Validation failed")
  })
  public ResponseEntity<RoutineResponse> createRoutine(
      @Valid @RequestBody CreateRoutineRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating routine name={} by user={}", request.name(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(routineService.createRoutine(request, currentUser));
  }

  @GetMapping
  @Operation(summary = "List routines for current organization")
  public ResponseEntity<List<RoutineResponse>> listRoutines(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String moduleType,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Boolean active
  ) {
    log.info("Listing routines for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        routineService.listRoutines(
            currentUser.getOrganization().getId(), moduleType, category, active));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a routine by ID")
  public ResponseEntity<RoutineResponse> getRoutine(@PathVariable Long id) {
    return ResponseEntity.ok(routineService.getRoutine(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update a routine")
  public ResponseEntity<RoutineResponse> updateRoutine(
      @PathVariable Long id,
      @Valid @RequestBody UpdateRoutineRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Updating routine id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.ok(routineService.updateRoutine(id, request, currentUser));
  }

  @PostMapping("/{id}/archive")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Archive a routine")
  public ResponseEntity<RoutineResponse> archiveRoutine(@PathVariable Long id) {
    log.info("Archiving routine id={}", id);
    return ResponseEntity.ok(routineService.archiveRoutine(id));
  }

  @PostMapping("/{id}/unarchive")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Unarchive a routine")
  public ResponseEntity<RoutineResponse> unarchiveRoutine(@PathVariable Long id) {
    log.info("Unarchiving routine id={}", id);
    return ResponseEntity.ok(routineService.unarchiveRoutine(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Delete a routine")
  public ResponseEntity<Void> deleteRoutine(@PathVariable Long id) {
    log.info("Deleting routine id={}", id);
    routineService.deleteRoutine(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/review")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Review a routine")
  public ResponseEntity<RoutineReviewResponse> reviewRoutine(
      @PathVariable Long id,
      @Valid @RequestBody ReviewRoutineRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Reviewing routine id={} by user={}", id, currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(routineService.reviewRoutine(id, request, currentUser));
  }

  @GetMapping("/{id}/history")
  @Operation(summary = "Get review history for a routine")
  public ResponseEntity<List<RoutineReviewResponse>> getRoutineHistory(@PathVariable Long id) {
    return ResponseEntity.ok(routineService.getRoutineHistory(id));
  }
}
