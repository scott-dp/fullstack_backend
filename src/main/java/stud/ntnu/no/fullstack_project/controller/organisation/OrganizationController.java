package stud.ntnu.no.fullstack_project.controller.organisation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.organization.OrganizationResponse;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.organization.OrganizationService;

/**
 * REST controller for organization listing.
 *
 * <p>Provides the organization list used by the superadmin area.</p>
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizations", description = "Endpoints for organization management")
public class OrganizationController {

  private final OrganizationService organizationService;

  @GetMapping
  @PreAuthorize("hasRole('SUPERADMIN')")
  @Operation(
      summary = "List all organizations",
      description = "Returns all registered organizations. Requires SUPERADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<OrganizationResponse>> listOrganizations() {
    log.info("Listing all organizations");
    return ResponseEntity.ok(organizationService.listOrganizations());
  }
}
