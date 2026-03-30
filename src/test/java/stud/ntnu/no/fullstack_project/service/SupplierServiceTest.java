package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service tests for supplier creation, updates, and active-state changes.
 */

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.supplier.*;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.Supplier;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.operations.SupplierRepository;
import stud.ntnu.no.fullstack_project.service.operations.SupplierService;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

  @Mock
  private SupplierRepository supplierRepository;

  @InjectMocks
  private SupplierService supplierService;

  private AppUser testUser;
  private Organization testOrg;

  @BeforeEach
  void setUp() {
    testOrg = new Organization();
    testOrg.setId(1L);
    testOrg.setName("Test Org");
    testOrg.setType(OrganizationType.RESTAURANT);

    testUser = new AppUser();
    testUser.setId(1L);
    testUser.setUsername("admin");
    testUser.setPassword("encoded");
    testUser.setOrganization(testOrg);
  }

  // --- Helper methods ---

  private Supplier buildSupplier(Long id, String name, boolean active) {
    Supplier supplier = new Supplier();
    supplier.setId(id);
    supplier.setOrganization(testOrg);
    supplier.setName(name);
    supplier.setOrganizationNumber("912345678");
    supplier.setContactName("Ola Nordmann");
    supplier.setEmail("kontakt@example.no");
    supplier.setPhone("+47 22 33 44 55");
    supplier.setAddress("Bryggen 1, Bergen");
    supplier.setActive(active);
    supplier.setCreatedAt(LocalDateTime.now());
    supplier.setUpdatedAt(LocalDateTime.now());
    return supplier;
  }

  // --- createSupplier tests ---

  @Test
  void createSupplier_validInput_createsSupplier() {
    CreateSupplierRequest request = new CreateSupplierRequest(
        "Norsk Sjømat AS", "912345678", "Ola Nordmann",
        "kontakt@norsksjømat.no", "+47 22 33 44 55", "Bryggen 1, Bergen", null
    );

    when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
      Supplier saved = invocation.getArgument(0);
      saved.setId(10L);
      saved.setCreatedAt(LocalDateTime.now());
      saved.setUpdatedAt(LocalDateTime.now());
      return saved;
    });

    SupplierResponse response = supplierService.createSupplier(request, testUser);

    assertNotNull(response);
    assertEquals(10L, response.id());
    assertEquals("Norsk Sjømat AS", response.name());
    assertEquals("912345678", response.organizationNumber());
    assertEquals("Ola Nordmann", response.contactName());
    assertTrue(response.active());
  }

  // --- getSupplier tests ---

  @Test
  void getSupplier_existingId_returnsSupplier() {
    Supplier supplier = buildSupplier(5L, "Test Supplier", true);
    when(supplierRepository.findById(5L)).thenReturn(Optional.of(supplier));

    SupplierResponse response = supplierService.getSupplier(5L);

    assertNotNull(response);
    assertEquals(5L, response.id());
    assertEquals("Test Supplier", response.name());
  }

  @Test
  void getSupplier_nonExistentId_throwsIllegalArgumentException() {
    when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> supplierService.getSupplier(999L));
  }

  // --- listSuppliers tests ---

  @Test
  void listSuppliers_returnsAllForOrg() {
    Supplier s1 = buildSupplier(1L, "Alpha Supplier", true);
    Supplier s2 = buildSupplier(2L, "Beta Supplier", true);

    when(supplierRepository.findByOrganizationIdOrderByNameAsc(1L))
        .thenReturn(List.of(s1, s2));

    List<SupplierResponse> result = supplierService.listSuppliers(1L);

    assertEquals(2, result.size());
    assertEquals("Alpha Supplier", result.get(0).name());
    assertEquals("Beta Supplier", result.get(1).name());
    verify(supplierRepository).findByOrganizationIdOrderByNameAsc(1L);
  }

  // --- updateSupplier tests ---

  @Test
  void updateSupplier_validInput_updatesFields() {
    Supplier supplier = buildSupplier(1L, "Old Name", true);
    when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
    when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));

    UpdateSupplierRequest request = new UpdateSupplierRequest(
        "New Name", null, null, null, null, null, null, false
    );

    SupplierResponse response = supplierService.updateSupplier(1L, request);

    assertEquals("New Name", response.name());
    assertFalse(response.active());
    verify(supplierRepository).save(any(Supplier.class));
  }

  @Test
  void updateSupplier_nonExistentId_throwsIllegalArgumentException() {
    when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

    UpdateSupplierRequest request = new UpdateSupplierRequest(
        "Name", null, null, null, null, null, null, null
    );

    assertThrows(IllegalArgumentException.class,
        () -> supplierService.updateSupplier(999L, request));
  }
}
