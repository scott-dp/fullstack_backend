package stud.ntnu.no.fullstack_project.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.health.HealthResponse;

/**
 * REST controller exposing a basic health-check endpoint.
 *
 * <p>This endpoint is publicly accessible and can be used by load balancers or
 * monitoring tools to verify that the application is running.</p>
 */
@RestController
@RequestMapping("/api/health")
@Slf4j
@Tag(name = "Health", description = "Basic application health endpoints")
public class HealthController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping
  @Operation(
      summary = "Return basic API health information",
      description = "Returns a simple status object indicating the application is running."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Application is healthy",
          content = @Content(schema = @Schema(implementation = HealthResponse.class)))
  })
  public ResponseEntity<HealthResponse> getHealth() {
    log.info("Health check requested");
    return ResponseEntity.ok(new HealthResponse("UP", applicationName));
  }
}
