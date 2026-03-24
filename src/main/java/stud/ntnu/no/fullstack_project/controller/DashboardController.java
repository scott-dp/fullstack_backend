package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.dashboard.DashboardResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard statistics")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  @Operation(summary = "Get dashboard statistics for the current user")
  public ResponseEntity<DashboardResponse> getDashboard(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.ok(dashboardService.getDashboard(currentUser));
  }
}
