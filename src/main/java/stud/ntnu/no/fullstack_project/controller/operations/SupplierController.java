package stud.ntnu.no.fullstack_project.controller.operations;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.supplier.CreateSupplierRequest;
import stud.ntnu.no.fullstack_project.dto.supplier.SupplierResponse;
import stud.ntnu.no.fullstack_project.dto.supplier.UpdateSupplierRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.SupplierService;

/**
 * REST controller for supplier management.
 *
 * <p>Provides endpoints to create, list, retrieve, and update suppliers
 * within the authenticated user's organization.</p>
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Suppliers", description = "Endpoints for supplier management")
public class SupplierController {

  private final SupplierService supplierService;

  @GetMapping
  @Operation(
      summary = "List suppliers for the current user's organization",
      description = "Returns all suppliers ordered by name."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully")
  })
  public ResponseEntity<List<SupplierResponse>> listSuppliers(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing suppliers for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        supplierService.listSuppliers(currentUser.getOrganization().getId()));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Create a new supplier",
      description = "Creates a new supplier for the current user's organization. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Supplier created successfully",
          content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<SupplierResponse> createSupplier(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Supplier details.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateSupplierRequest.class),
              examples = @ExampleObject(name = "Create supplier", value = """
                  {
                    "name": "Norsk Sjømat AS",
                    "organizationNumber": "912345678",
                    "contactName": "Ola Nordmann",
                    "email": "kontakt@norsksjømat.no",
                    "phone": "+47 22 33 44 55"
                  }
                  """)))
      @Valid @RequestBody CreateSupplierRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating supplier name={} by user={}", request.name(), currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(supplierService.createSupplier(request, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a supplier by ID",
      description = "Returns a single supplier."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Supplier found",
          content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
      @ApiResponse(responseCode = "400", description = "Supplier not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long id) {
    log.info("Fetching supplier id={}", id);
    return ResponseEntity.ok(supplierService.getSupplier(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Update a supplier",
      description = "Updates an existing supplier. Requires ADMIN or MANAGER role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Supplier updated successfully",
          content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or supplier not found",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<SupplierResponse> updateSupplier(
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Fields to update on the supplier.",
          required = true,
          content = @Content(schema = @Schema(implementation = UpdateSupplierRequest.class),
              examples = @ExampleObject(name = "Update supplier", value = """
                  {"name": "Updated Supplier Name", "active": false}
                  """)))
      @Valid @RequestBody UpdateSupplierRequest request
  ) {
    log.info("Updating supplier id={}", id);
    return ResponseEntity.ok(supplierService.updateSupplier(id, request));
  }
}
