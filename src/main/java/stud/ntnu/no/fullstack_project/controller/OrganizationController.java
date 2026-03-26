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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.organization.CreateOrganizationRequest;
import stud.ntnu.no.fullstack_project.dto.organization.OrganizationResponse;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.OrganizationService;

/**
 * REST controller for organization management.
 *
 * <p>Provides CRUD operations for organizations. Creation and listing of all
 * organizations require SUPERADMIN role; updates require ADMIN or MANAGER.</p>
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizations", description = "Endpoints for organization management")
public class OrganizationController {

  private final OrganizationService organizationService;

  @PostMapping
  @PreAuthorize("hasRole('SUPERADMIN')")
  @Operation(
      summary = "Create a new organization",
      description = "Creates a new organization record. Requires SUPERADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Organization created successfully",
          content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or organization number already exists",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<OrganizationResponse> createOrganization(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Organization details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateOrganizationRequest.class),
              examples = @ExampleObject(name = "Create organization", value = """
                  {
                    "name": "Everest Sushi & Fusion",
                    "organizationNumber": "937219997",
                    "address": "Trondheim, Norway",
                    "phone": "+47 123 45 678",
                    "type": "RESTAURANT"
                  }
                  """)))
      @Valid @RequestBody CreateOrganizationRequest request
  ) {
    log.info("Creating organization name={}", request.name());
    return ResponseEntity.ok(organizationService.createOrganization(request));
  }

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

  @GetMapping("/{id}")
  @Operation(
      summary = "Get an organization by ID",
      description = "Returns a single organization by its unique identifier."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Organization found",
          content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Organization not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable Long id) {
    log.info("Fetching organization id={}", id);
    return ResponseEntity.ok(organizationService.getOrganization(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update an organization",
      description = "Updates an existing organization's details. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Organization updated successfully",
          content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or organization not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<OrganizationResponse> updateOrganization(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Updated organization details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateOrganizationRequest.class)))
      @Valid @RequestBody CreateOrganizationRequest request
  ) {
    log.info("Updating organization id={}", id);
    return ResponseEntity.ok(organizationService.updateOrganization(id, request));
  }
}
