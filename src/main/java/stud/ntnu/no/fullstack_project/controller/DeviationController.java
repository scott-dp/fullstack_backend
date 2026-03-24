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
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.DeviationService;

@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
@Tag(name = "Deviations", description = "Endpoints for deviation reporting and management")
public class DeviationController {

  private final DeviationService deviationService;

  @PostMapping
  @Operation(summary = "Create a new deviation")
  public ResponseEntity<DeviationResponse> createDeviation(
      @Valid @RequestBody CreateDeviationRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deviationService.createDeviation(request, currentUser));
  }

  @GetMapping
  @Operation(summary = "List deviations for the current user's organization")
  public ResponseEntity<List<DeviationResponse>> listDeviations(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category
  ) {
    return ResponseEntity.ok(
        deviationService.listDeviations(
            currentUser.getOrganization().getId(), status, category
        )
    );
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a deviation by ID with comments")
  public ResponseEntity<DeviationResponse> getDeviation(@PathVariable Long id) {
    return ResponseEntity.ok(deviationService.getDeviation(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update a deviation's status or assignment")
  public ResponseEntity<DeviationResponse> updateDeviation(
      @PathVariable Long id,
      @Valid @RequestBody UpdateDeviationRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(deviationService.updateDeviation(id, request, currentUser));
  }

  @PostMapping("/{id}/comments")
  @Operation(summary = "Add a comment to a deviation")
  public ResponseEntity<DeviationCommentResponse> addComment(
      @PathVariable Long id,
      @Valid @RequestBody AddDeviationCommentRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deviationService.addComment(id, request, currentUser));
  }
}
