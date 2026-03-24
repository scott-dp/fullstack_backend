package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.temperature.CreateTemperatureLogRequest;
import stud.ntnu.no.fullstack_project.dto.temperature.TemperatureLogResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.TemperatureLogService;

@RestController
@RequestMapping("/api/temperature-logs")
@RequiredArgsConstructor
@Tag(name = "Temperature Logs", description = "Endpoints for temperature log management")
public class TemperatureLogController {

  private final TemperatureLogService temperatureLogService;

  @PostMapping
  @Operation(summary = "Create a new temperature log")
  public ResponseEntity<TemperatureLogResponse> createLog(
      @Valid @RequestBody CreateTemperatureLogRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(temperatureLogService.createLog(request, currentUser));
  }

  @GetMapping
  @Operation(summary = "List temperature logs for the current user's organization")
  public ResponseEntity<List<TemperatureLogResponse>> listLogs(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String location
  ) {
    return ResponseEntity.ok(
        temperatureLogService.listLogs(currentUser.getOrganization().getId(), location)
    );
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a temperature log by ID")
  public ResponseEntity<TemperatureLogResponse> getLog(@PathVariable Long id) {
    return ResponseEntity.ok(temperatureLogService.getLog(id));
  }
}
