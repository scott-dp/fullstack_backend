package stud.ntnu.no.fullstack_project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.delivery.CreateDeliveryItemRequest;
import stud.ntnu.no.fullstack_project.dto.delivery.CreateDeliveryRequest;
import stud.ntnu.no.fullstack_project.dto.delivery.DeliveryItemResponse;
import stud.ntnu.no.fullstack_project.dto.delivery.DeliveryRecordResponse;
import stud.ntnu.no.fullstack_project.dto.delivery.TraceabilitySearchResponse;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.DeliveryItem;
import stud.ntnu.no.fullstack_project.entity.DeliveryRecord;
import stud.ntnu.no.fullstack_project.entity.Supplier;
import stud.ntnu.no.fullstack_project.repository.DeliveryItemRepository;
import stud.ntnu.no.fullstack_project.repository.DeliveryRecordRepository;
import stud.ntnu.no.fullstack_project.repository.SupplierRepository;

/**
 * Service for managing delivery records and traceability searches.
 *
 * <p>Handles creation, retrieval, and listing of delivery records with their
 * line items, as well as traceability searches by product name or batch/lot.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRecordRepository deliveryRecordRepository;
  private final DeliveryItemRepository deliveryItemRepository;
  private final SupplierRepository supplierRepository;

  /**
   * Creates a new delivery record with its line items.
   *
   * @param request     the delivery details including items
   * @param currentUser the authenticated user recording the delivery
   * @return the created delivery record response
   */
  @Transactional
  public DeliveryRecordResponse createDelivery(CreateDeliveryRequest request,
      AppUser currentUser) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Supplier not found with id: " + request.supplierId()));

    DeliveryRecord record = new DeliveryRecord();
    record.setOrganization(currentUser.getOrganization());
    record.setSupplier(supplier);
    record.setDeliveryDate(LocalDate.parse(request.deliveryDate()));
    record.setDocumentNumber(request.documentNumber());
    record.setReceivedBy(currentUser);
    record.setNotes(request.notes());
    record.setAttachmentId(request.attachmentId());

    for (CreateDeliveryItemRequest itemReq : request.items()) {
      DeliveryItem item = new DeliveryItem();
      item.setDeliveryRecord(record);
      item.setProductName(itemReq.productName());
      item.setQuantity(itemReq.quantity());
      item.setUnit(itemReq.unit());
      item.setBatchLot(itemReq.batchLot());
      if (itemReq.expiryDate() != null && !itemReq.expiryDate().isBlank()) {
        item.setExpiryDate(LocalDate.parse(itemReq.expiryDate()));
      }
      item.setInternalIngredientRef(itemReq.internalIngredientRef());
      record.getItems().add(item);
    }

    DeliveryRecord saved = deliveryRecordRepository.save(record);
    log.info("Delivery record created: id={}, supplier={}", saved.getId(), supplier.getName());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a delivery record by its ID, including line items.
   *
   * @param id the delivery record identifier
   * @return the delivery record response
   */
  public DeliveryRecordResponse getDelivery(Long id) {
    DeliveryRecord record = deliveryRecordRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Delivery record not found with id: " + id));
    return mapToResponse(record);
  }

  /**
   * Lists all delivery records for an organization, ordered by delivery date descending.
   *
   * @param organizationId the organization identifier
   * @return list of delivery record responses
   */
  public List<DeliveryRecordResponse> listDeliveries(Long organizationId) {
    return deliveryRecordRepository.findByOrganizationIdOrderByDeliveryDateDesc(organizationId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Searches delivery items for traceability by product name or batch/lot number.
   *
   * @param organizationId the organization identifier
   * @param productName    optional product name search term
   * @param batchLot       optional batch/lot search term
   * @return list of traceability search results
   */
  public List<TraceabilitySearchResponse> searchTraceability(Long organizationId,
      String productName, String batchLot) {
    List<DeliveryItem> items;

    if (productName != null && !productName.isBlank()) {
      items = deliveryItemRepository
          .findByDeliveryRecordOrganizationIdAndProductNameContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
              organizationId, productName);
    } else if (batchLot != null && !batchLot.isBlank()) {
      items = deliveryItemRepository
          .findByDeliveryRecordOrganizationIdAndBatchLotContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
              organizationId, batchLot);
    } else {
      return List.of();
    }

    return items.stream()
        .map(this::mapToTraceabilityResponse)
        .collect(Collectors.toList());
  }

  /**
   * Maps a delivery record entity to its response DTO.
   *
   * @param record the delivery record entity
   * @return the delivery record response DTO
   */
  private DeliveryRecordResponse mapToResponse(DeliveryRecord record) {
    List<DeliveryItemResponse> itemResponses = record.getItems().stream()
        .map(this::mapToItemResponse)
        .collect(Collectors.toList());

    return new DeliveryRecordResponse(
        record.getId(),
        record.getSupplier().getId(),
        record.getSupplier().getName(),
        record.getDeliveryDate(),
        record.getDocumentNumber(),
        record.getReceivedBy().getUsername(),
        record.getNotes(),
        record.getAttachmentId(),
        itemResponses,
        record.getCreatedAt()
    );
  }

  /**
   * Maps a delivery item entity to its response DTO.
   *
   * @param item the delivery item entity
   * @return the delivery item response DTO
   */
  private DeliveryItemResponse mapToItemResponse(DeliveryItem item) {
    return new DeliveryItemResponse(
        item.getId(),
        item.getProductName(),
        item.getQuantity(),
        item.getUnit(),
        item.getBatchLot(),
        item.getExpiryDate(),
        item.getInternalIngredientRef()
    );
  }

  /**
   * Maps a delivery item entity to a traceability search response DTO.
   *
   * @param item the delivery item entity
   * @return the traceability search response DTO
   */
  private TraceabilitySearchResponse mapToTraceabilityResponse(DeliveryItem item) {
    DeliveryRecord record = item.getDeliveryRecord();
    return new TraceabilitySearchResponse(
        item.getId(),
        record.getId(),
        record.getSupplier().getName(),
        item.getProductName(),
        item.getBatchLot(),
        record.getDeliveryDate(),
        item.getExpiryDate()
    );
  }
}
