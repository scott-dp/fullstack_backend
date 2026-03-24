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
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.TemperatureLogService;

/**
 * REST controller for temperature log management.
 *
 * <p>Provides endpoints for recording temperature measurements and reviewing
 * historical logs, with automatic threshold-based status calculation.</p>
 */
@RestController
@RequestMapping("/api/temperature-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Temperature Logs", description = "Endpoints for temperature log management")
public class TemperatureLogController {

  private final TemperatureLogService temperatureLogService;

  @PostMapping
  @Operation(
      summary = "Create a new temperature log",
      description = "Records a temperature measurement and automatically calculates the status "
          + "based on the provided thresholds."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Temperature log created successfully",
          content = @Content(schema = @Schema(implementation = TemperatureLogResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TemperatureLogResponse> createLog(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Temperature measurement details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateTemperatureLogRequest.class),
              examples = @ExampleObject(name = "Create temperature log", value = """
                  {
                    "location": "Walk-in Fridge",
                    "temperature": 3.5,
                    "minThreshold": 0.0,
                    "maxThreshold": 4.0,
                    "comment": "Measured after restocking."
                  }
                  """)))
      @Valid @RequestBody CreateTemperatureLogRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating temperature log location={} by user={}", request.location(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(temperatureLogService.createLog(request, currentUser));
  }

  @GetMapping
  @Operation(
      summary = "List temperature logs for the current user's organization",
      description = "Returns temperature logs optionally filtered by location, ordered by most recent first."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Temperature logs retrieved successfully")
  })
  public ResponseEntity<List<TemperatureLogResponse>> listLogs(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String location
  ) {
    log.info("Listing temperature logs for orgId={}, location={}", currentUser.getOrganization().getId(), location);
    return ResponseEntity.ok(
        temperatureLogService.listLogs(currentUser.getOrganization().getId(), location)
    );
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a temperature log by ID",
      description = "Returns a single temperature log entry."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Temperature log found",
          content = @Content(schema = @Schema(implementation = TemperatureLogResponse.class))),
      @ApiResponse(responseCode = "400", description = "Temperature log not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<TemperatureLogResponse> getLog(@PathVariable Long id) {
    log.info("Fetching temperature log id={}", id);
    return ResponseEntity.ok(temperatureLogService.getLog(id));
  }
}
