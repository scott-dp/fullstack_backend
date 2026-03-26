package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenResponse;
import stud.ntnu.no.fullstack_project.service.AllergenService;

/**
 * REST controller for allergen reference data.
 *
 * <p>Provides read-only access to the 14 EU-defined allergens.</p>
 */
@RestController
@RequestMapping("/api/allergens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Allergens", description = "Endpoints for allergen reference data")
public class AllergenController {

  private final AllergenService allergenService;

  @GetMapping
  @Operation(
      summary = "List all active allergens",
      description = "Returns all 14 EU-defined allergens that are currently active."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Allergens retrieved successfully")
  })
  public ResponseEntity<List<AllergenResponse>> listAllergens() {
    log.info("Listing all active allergens");
    return ResponseEntity.ok(allergenService.listAllergens());
  }
}
