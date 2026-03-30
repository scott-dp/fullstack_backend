package stud.ntnu.no.fullstack_project.controller.admin;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.superadmin.CreateOrganizationAdminRequest;
import stud.ntnu.no.fullstack_project.dto.superadmin.OrganizationAdminSummaryResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.admin.SuperAdminService;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPERADMIN')")
@Tag(name = "Superadmin", description = "Global multi-organization administration endpoints")
public class SuperAdminController {

  private final SuperAdminService superAdminService;

  @PostMapping("/organization-admins")
  @Operation(
      summary = "Create an organization and invite its first admin",
      description = "Creates a new organization, provisions a pending org-admin account, and emails a setup link."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Organization admin invited successfully",
          content = @Content(schema = @Schema(implementation = OrganizationAdminSummaryResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<OrganizationAdminSummaryResponse> createOrganizationAdmin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(schema = @Schema(implementation = CreateOrganizationAdminRequest.class),
              examples = @ExampleObject(value = """
                  {
                    "organizationName": "North Peak Bistro",
                    "organizationNumber": "123456789",
                    "organizationType": "RESTAURANT",
                    "firstName": "Ava",
                    "lastName": "Nilsen",
                    "email": "ava@example.com"
                  }
                  """)))
      @Valid @RequestBody CreateOrganizationAdminRequest request
  ) {
    log.info("Superadmin creating organization={} and admin email={}", request.organizationName(), request.email());
    return ResponseEntity.ok(superAdminService.createOrganizationWithAdmin(request));
  }

  @GetMapping("/organization-admins")
  @Operation(summary = "List all organization admins")
  public ResponseEntity<List<OrganizationAdminSummaryResponse>> listOrganizationAdmins() {
    log.info("Superadmin listing organization admins");
    return ResponseEntity.ok(superAdminService.listOrganizationAdmins());
  }

  @DeleteMapping("/organization-admins/{id}")
  @Operation(summary = "Archive an organization admin")
  public ResponseEntity<Void> archiveOrganizationAdmin(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Superadmin archiving organization admin id={}", id);
    superAdminService.archiveOrganizationAdmin(id, currentUser);
    return ResponseEntity.noContent().build();
  }
}
