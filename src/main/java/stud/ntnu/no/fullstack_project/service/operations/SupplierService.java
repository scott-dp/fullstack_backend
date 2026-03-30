package stud.ntnu.no.fullstack_project.service.operations;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.supplier.CreateSupplierRequest;
import stud.ntnu.no.fullstack_project.dto.supplier.SupplierResponse;
import stud.ntnu.no.fullstack_project.dto.supplier.UpdateSupplierRequest;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.Supplier;
import stud.ntnu.no.fullstack_project.repository.operations.SupplierRepository;

/**
 * Service for managing suppliers.
 *
 * <p>Handles creation, retrieval, listing, updating, and activation/deactivation
 * of suppliers within an organization.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

  private final SupplierRepository supplierRepository;

  /**
   * Creates a new supplier for the current user's organization.
   *
   * @param request     the supplier details
   * @param currentUser the authenticated user
   * @return the created supplier response
   */
  @Transactional
  public SupplierResponse createSupplier(CreateSupplierRequest request, AppUser currentUser) {
    Supplier supplier = new Supplier();
    supplier.setOrganization(currentUser.getOrganization());
    supplier.setName(request.name());
    supplier.setOrganizationNumber(request.organizationNumber());
    supplier.setContactName(request.contactName());
    supplier.setEmail(request.email());
    supplier.setPhone(request.phone());
    supplier.setAddress(request.address());
    supplier.setNotes(request.notes());

    Supplier saved = supplierRepository.save(supplier);
    log.info("Supplier created: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a supplier by its ID.
   *
   * @param id the supplier identifier
   * @return the supplier response
   */
  public SupplierResponse getSupplier(Long id) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found with id: " + id));
    return mapToResponse(supplier);
  }

  /**
   * Lists all suppliers for an organization, ordered by name.
   *
   * @param organizationId the organization identifier
   * @return list of supplier responses
   */
  public List<SupplierResponse> listSuppliers(Long organizationId) {
    return supplierRepository.findByOrganizationIdOrderByNameAsc(organizationId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an existing supplier.
   *
   * @param id      the supplier identifier
   * @param request the fields to update
   * @return the updated supplier response
   */
  @Transactional
  public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found with id: " + id));

    if (request.name() != null && !request.name().isBlank()) {
      supplier.setName(request.name());
    }
    if (request.organizationNumber() != null) {
      supplier.setOrganizationNumber(request.organizationNumber());
    }
    if (request.contactName() != null) {
      supplier.setContactName(request.contactName());
    }
    if (request.email() != null) {
      supplier.setEmail(request.email());
    }
    if (request.phone() != null) {
      supplier.setPhone(request.phone());
    }
    if (request.address() != null) {
      supplier.setAddress(request.address());
    }
    if (request.notes() != null) {
      supplier.setNotes(request.notes());
    }
    if (request.active() != null) {
      supplier.setActive(request.active());
    }

    Supplier saved = supplierRepository.save(supplier);
    log.info("Supplier updated: id={}", saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Maps a supplier entity to its response DTO.
   *
   * @param supplier the supplier entity
   * @return the supplier response DTO
   */
  private SupplierResponse mapToResponse(Supplier supplier) {
    return new SupplierResponse(
        supplier.getId(),
        supplier.getName(),
        supplier.getOrganizationNumber(),
        supplier.getContactName(),
        supplier.getEmail(),
        supplier.getPhone(),
        supplier.getAddress(),
        supplier.getNotes(),
        supplier.isActive(),
        supplier.getCreatedAt(),
        supplier.getUpdatedAt()
    );
  }
}
