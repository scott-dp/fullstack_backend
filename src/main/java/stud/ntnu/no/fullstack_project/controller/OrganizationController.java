package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import stud.ntnu.no.fullstack_project.service.OrganizationService;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Endpoints for organization management")
public class OrganizationController {

  private final OrganizationService organizationService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a new organization")
  public ResponseEntity<OrganizationResponse> createOrganization(
      @Valid @RequestBody CreateOrganizationRequest request
  ) {
    return ResponseEntity.ok(organizationService.createOrganization(request));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List all organizations")
  public ResponseEntity<List<OrganizationResponse>> listOrganizations() {
    return ResponseEntity.ok(organizationService.listOrganizations());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an organization by ID")
  public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable Long id) {
    return ResponseEntity.ok(organizationService.getOrganization(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update an organization")
  public ResponseEntity<OrganizationResponse> updateOrganization(
      @PathVariable Long id,
      @Valid @RequestBody CreateOrganizationRequest request
  ) {
    return ResponseEntity.ok(organizationService.updateOrganization(id, request));
  }
}
