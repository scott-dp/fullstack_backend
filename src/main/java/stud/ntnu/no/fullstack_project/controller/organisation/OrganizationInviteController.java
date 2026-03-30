package stud.ntnu.no.fullstack_project.controller.organisation;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.invite.AcceptOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.CreateOrganizationInviteRequest;
import stud.ntnu.no.fullstack_project.dto.invite.OrganizationInviteResponse;
import stud.ntnu.no.fullstack_project.dto.user.CurrentUserResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.organization.OrganizationInviteService;

/**
 * REST controller exposing organization invitation management endpoints.
 */
@RestController
@RequestMapping("/api/organization-invites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organization Invitations", description = "Endpoints for creating and accepting organization join invites")
public class OrganizationInviteController {

  private final OrganizationInviteService organizationInviteService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "List visible organization invites",
      description = "Admins receive all invites. Managers receive invites for their own organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Invites returned successfully"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<List<OrganizationInviteResponse>> listInvites(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing organization invites for user={}", currentUser.getUsername());
    return ResponseEntity.ok(organizationInviteService.listVisibleInvites(currentUser));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a new organization invite",
      description = "Admins can create manager or staff invites for any organization. "
          + "Managers can create staff invites for their own organization."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Invite created successfully",
          content = @Content(schema = @Schema(implementation = OrganizationInviteResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid role, organization, or permission scope",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<OrganizationInviteResponse> createInvite(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Invitation parameters.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateOrganizationInviteRequest.class),
              examples = @ExampleObject(name = "Manager invite", value = """
                  {
                    "role": "ROLE_MANAGER",
                    "organizationId": 1,
                    "expiresInDays": 7
                  }
                  """)))
      @Valid @RequestBody CreateOrganizationInviteRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating organization invite role={} by user={}", request.role(), currentUser.getUsername());
    return ResponseEntity.ok(organizationInviteService.createInvite(request, currentUser));
  }

  @PostMapping("/accept")
  @Operation(
      summary = "Accept an organization invite",
      description = "Allows a logged-in user without an organization to join the invited restaurant "
          + "and receive the invited role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Invite accepted successfully",
          content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invite is invalid, expired, or already used",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<CurrentUserResponse> acceptInvite(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Invitation token payload.",
          required = true,
          content = @Content(schema = @Schema(implementation = AcceptOrganizationInviteRequest.class),
              examples = @ExampleObject(name = "Accept invite", value = """
                  {
                    "token": "4d27358d-fd4c-4258-bc66-2fd197b56e66"
                  }
                  """)))
      @Valid @RequestBody AcceptOrganizationInviteRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Accepting organization invite for user={}", currentUser.getUsername());
    return ResponseEntity.ok(organizationInviteService.acceptInvite(request, currentUser));
  }
}
