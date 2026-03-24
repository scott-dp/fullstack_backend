package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.dashboard.DashboardResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.DashboardService;

/**
 * REST controller for retrieving aggregated dashboard statistics.
 *
 * <p>Returns key metrics for the authenticated user's organization, such as
 * checklist completions, temperature alerts, and open deviations.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Endpoints for dashboard statistics")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  @Operation(
      summary = "Get dashboard statistics for the current user",
      description = "Returns aggregated metrics including checklist counts, temperature alerts, "
          + "deviation counts, and unread notifications for the user's organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully",
          content = @Content(schema = @Schema(implementation = DashboardResponse.class)))
  })
  public ResponseEntity<DashboardResponse> getDashboard(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Dashboard requested by user={}", currentUser.getUsername());
    return ResponseEntity.ok(dashboardService.getDashboard(currentUser));
  }
}
