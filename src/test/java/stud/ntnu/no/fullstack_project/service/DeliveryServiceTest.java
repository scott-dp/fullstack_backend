package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for delivery recording and traceability lookup behavior.
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.delivery.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.DeliveryItem;
import stud.ntnu.no.fullstack_project.entity.operations.DeliveryRecord;
import stud.ntnu.no.fullstack_project.entity.operations.Supplier;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.operations.DeliveryItemRepository;
import stud.ntnu.no.fullstack_project.repository.operations.DeliveryRecordRepository;
import stud.ntnu.no.fullstack_project.repository.operations.SupplierRepository;
import stud.ntnu.no.fullstack_project.service.operations.DeliveryService;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock
  private DeliveryRecordRepository deliveryRecordRepository;

  @Mock
  private DeliveryItemRepository deliveryItemRepository;

  @Mock
  private SupplierRepository supplierRepository;

  @InjectMocks
  private DeliveryService deliveryService;

  private AppUser testUser;
  private Organization testOrg;
  private Supplier testSupplier;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("staff");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);

    testSupplier = new Supplier();
    testSupplier.setId(1L);
    testSupplier.setOrganization(testOrg);
    testSupplier.setName("Norsk Sjømat AS");
  }

  // --- Helper methods ---

  private DeliveryRecord buildDeliveryRecord(Long id) {
    DeliveryRecord record = new DeliveryRecord();
    record.setId(id);
    record.setOrganization(testOrg);
    record.setSupplier(testSupplier);
    record.setDeliveryDate(LocalDate.of(2025, 2, 20));
    record.setDocumentNumber("INV-2025-100");
    record.setReceivedBy(testUser);
    record.setCreatedAt(LocalDateTime.now());

    DeliveryItem item = new DeliveryItem();
    item.setId(1L);
    item.setDeliveryRecord(record);
    item.setProductName("Atlantic Salmon Fillet");
    item.setQuantity("50");
    item.setUnit("kg");
    item.setBatchLot("LOT-2025-0042");
    item.setExpiryDate(LocalDate.of(2025, 3, 15));
    record.getItems().add(item);

    return record;
  }

  // --- createDelivery tests ---

  @Test
  void createDelivery_validInput_createsDeliveryWithItems() {
    CreateDeliveryItemRequest itemReq = new CreateDeliveryItemRequest(
        "Atlantic Salmon Fillet", "50", "kg", "LOT-2025-0042", "2025-03-15", null
    );
    CreateDeliveryRequest request = new CreateDeliveryRequest(
        1L, "2025-02-20", "INV-2025-100", null, List.of(itemReq)
    );

    when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
    when(deliveryRecordRepository.save(any(DeliveryRecord.class))).thenAnswer(invocation -> {
      DeliveryRecord saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      if (!saved.getItems().isEmpty()) {
        saved.getItems().get(0).setId(1L);
      }
      return saved;
    });

    DeliveryRecordResponse response = deliveryService.createDelivery(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals(1L, response.supplierId());
    assertEquals("Norsk Sjømat AS", response.supplierName());
    assertEquals(LocalDate.of(2025, 2, 20), response.deliveryDate());
    assertEquals("INV-2025-100", response.documentNumber());
    assertEquals("staff", response.receivedByUsername());
    assertEquals(1, response.items().size());
    assertEquals("Atlantic Salmon Fillet", response.items().get(0).productName());
    assertEquals("LOT-2025-0042", response.items().get(0).batchLot());
  }

  @Test
  void createDelivery_supplierNotFound_throwsIllegalArgumentException() {
    CreateDeliveryRequest request = new CreateDeliveryRequest(
        999L, "2025-02-20", null, null, List.of()
    );

    when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> deliveryService.createDelivery(request, testUser));
  }

  // --- getDelivery tests ---

  @Test
  void getDelivery_existingId_returnsDeliveryWithItems() {
    DeliveryRecord record = buildDeliveryRecord(5L);
    when(deliveryRecordRepository.findById(5L)).thenReturn(Optional.of(record));

    DeliveryRecordResponse response = deliveryService.getDelivery(5L);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals(1, response.items().size());
    assertEquals("Atlantic Salmon Fillet", response.items().get(0).productName());
  }

  @Test
  void getDelivery_nonExistentId_throwsIllegalArgumentException() {
    when(deliveryRecordRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> deliveryService.getDelivery(999L));
  }

  // --- searchTraceability tests ---

  @Test
  void searchTraceability_byProductName_returnsResults() {
    DeliveryRecord record = buildDeliveryRecord(1L);
    DeliveryItem item = record.getItems().get(0);

    when(deliveryItemRepository
        .findByDeliveryRecordOrganizationIdAndProductNameContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
            1L, "salmon"))
        .thenReturn(List.of(item));

    List<TraceabilitySearchResponse> results =
        deliveryService.searchTraceability(1L, "salmon", null);

    assertEquals(1, results.size());
    assertEquals("Atlantic Salmon Fillet", results.get(0).productName());
    assertEquals("LOT-2025-0042", results.get(0).batchLot());
    assertEquals("Norsk Sjømat AS", results.get(0).supplierName());
  }

  @Test
  void searchTraceability_byBatchLot_returnsResults() {
    DeliveryRecord record = buildDeliveryRecord(1L);
    DeliveryItem item = record.getItems().get(0);

    when(deliveryItemRepository
        .findByDeliveryRecordOrganizationIdAndBatchLotContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
            1L, "LOT-2025"))
        .thenReturn(List.of(item));

    List<TraceabilitySearchResponse> results =
        deliveryService.searchTraceability(1L, null, "LOT-2025");

    assertEquals(1, results.size());
    assertEquals("LOT-2025-0042", results.get(0).batchLot());
  }

  @Test
  void searchTraceability_noParams_returnsEmpty() {
    List<TraceabilitySearchResponse> results =
        deliveryService.searchTraceability(1L, null, null);

    assertTrue(results.isEmpty());
  }
}
