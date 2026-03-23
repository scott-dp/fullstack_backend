package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.health.HealthResponse;

/**
 * Public controller exposing lightweight operational status endpoints.
 *
 * <p>The health endpoint is intentionally unauthenticated so frontend and deployment
 * tooling can verify that the API is reachable.</p>
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Basic application health endpoints")
public class HealthController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping
  @Operation(
      summary = "Return basic API health information",
      description = "Returns a lightweight status payload showing that the backend is up "
          + "and identifying the configured application name."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Health payload returned successfully",
          content = @Content(schema = @Schema(implementation = HealthResponse.class))
      )
  })
  public ResponseEntity<HealthResponse> getHealth() {
    return ResponseEntity.ok(new HealthResponse("UP", applicationName));
  }
}
