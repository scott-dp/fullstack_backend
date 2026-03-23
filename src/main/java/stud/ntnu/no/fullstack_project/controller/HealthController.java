package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.health.HealthResponse;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Basic application health endpoints")
public class HealthController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping
  @Operation(summary = "Return basic API health information")
  public ResponseEntity<HealthResponse> getHealth() {
    return ResponseEntity.ok(new HealthResponse("UP", applicationName));
  }
}
