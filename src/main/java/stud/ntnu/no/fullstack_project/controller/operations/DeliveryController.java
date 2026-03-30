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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.delivery.CreateDeliveryRequest;
import stud.ntnu.no.fullstack_project.dto.delivery.DeliveryRecordResponse;
import stud.ntnu.no.fullstack_project.dto.delivery.TraceabilitySearchResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.exception.ApiError;
import stud.ntnu.no.fullstack_project.service.operations.DeliveryService;

/**
 * REST controller for delivery record management and traceability.
 *
 * <p>Provides endpoints to create, list, and retrieve delivery records,
 * as well as search for traceability information.</p>
 */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deliveries", description = "Endpoints for delivery record management and traceability")
public class DeliveryController {

  private final DeliveryService deliveryService;

  @GetMapping
  @Operation(
      summary = "List delivery records for the current user's organization",
      description = "Returns all delivery records ordered by delivery date descending."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Delivery records retrieved successfully")
  })
  public ResponseEntity<List<DeliveryRecordResponse>> listDeliveries(
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Listing deliveries for orgId={}", currentUser.getOrganization().getId());
    return ResponseEntity.ok(
        deliveryService.listDeliveries(currentUser.getOrganization().getId()));
  }

  @PostMapping
  @Operation(
      summary = "Create a new delivery record",
      description = "Records a new delivery with line items."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Delivery record created successfully",
          content = @Content(schema = @Schema(implementation = DeliveryRecordResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation failed or supplier not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeliveryRecordResponse> createDelivery(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Delivery details with line items.",
          required = true,
          content = @Content(schema = @Schema(implementation = CreateDeliveryRequest.class),
              examples = @ExampleObject(name = "Create delivery", value = """
                  {
                    "supplierId": 1,
                    "deliveryDate": "2025-02-20",
                    "documentNumber": "INV-2025-100",
                    "items": [
                      {
                        "productName": "Atlantic Salmon Fillet",
                        "quantity": "50",
                        "unit": "kg",
                        "batchLot": "LOT-2025-0042",
                        "expiryDate": "2025-03-15"
                      }
                    ]
                  }
                  """)))
      @Valid @RequestBody CreateDeliveryRequest request,
      @AuthenticationPrincipal AppUser currentUser
  ) {
    log.info("Creating delivery for supplier={} by user={}", request.supplierId(),
        currentUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deliveryService.createDelivery(request, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a delivery record by ID",
      description = "Returns a single delivery record with its line items."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Delivery record found",
          content = @Content(schema = @Schema(implementation = DeliveryRecordResponse.class))),
      @ApiResponse(responseCode = "400", description = "Delivery record not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public ResponseEntity<DeliveryRecordResponse> getDelivery(@PathVariable Long id) {
    log.info("Fetching delivery id={}", id);
    return ResponseEntity.ok(deliveryService.getDelivery(id));
  }

  @GetMapping("/search/traceability")
  @Operation(
      summary = "Search delivery items for traceability",
      description = "Searches delivery items by product name or batch/lot number."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
  })
  public ResponseEntity<List<TraceabilitySearchResponse>> searchTraceability(
      @AuthenticationPrincipal AppUser currentUser,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) String batchLot,
      @RequestParam(required = false) Long supplierId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo
  ) {
    log.info("Traceability search for orgId={}, productName={}, batchLot={}",
        currentUser.getOrganization().getId(), productName, batchLot);
    return ResponseEntity.ok(
        deliveryService.searchTraceability(
            currentUser.getOrganization().getId(), productName, batchLot));
  }
}
