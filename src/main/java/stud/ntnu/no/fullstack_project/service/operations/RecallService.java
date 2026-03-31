package stud.ntnu.no.fullstack_project.service.operations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.recall.CreateRecallRequest;
import stud.ntnu.no.fullstack_project.dto.recall.RecallCaseResponse;
import stud.ntnu.no.fullstack_project.entity.auth.AppUser;
import stud.ntnu.no.fullstack_project.entity.operations.RecallCase;
import stud.ntnu.no.fullstack_project.entity.operations.RecallStatus;
import stud.ntnu.no.fullstack_project.entity.operations.Supplier;
import stud.ntnu.no.fullstack_project.repository.operations.RecallCaseRepository;
import stud.ntnu.no.fullstack_project.repository.operations.SupplierRepository;

/**
 * Service for managing recall cases.
 *
 * <p>Handles creation, retrieval, and listing of product recall cases
 * within an organization.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecallService {

  private final RecallCaseRepository recallCaseRepository;
  private final SupplierRepository supplierRepository;

  /**
   * Creates a new recall case for the current user's organization.
   *
   * @param request     the recall case details
   * @param currentUser the authenticated user creating the recall
   * @return the created recall case response
   */
  @Transactional
  public RecallCaseResponse createRecall(CreateRecallRequest request, AppUser currentUser) {
    RecallCase recallCase = new RecallCase();
    recallCase.setOrganization(currentUser.getOrganization());
    recallCase.setTitle(request.title());
    recallCase.setProductName(request.productName());
    recallCase.setBatchLot(request.batchLot());
    recallCase.setDescription(request.description());
    recallCase.setOpenedAt(LocalDateTime.now());
    recallCase.setStatus(RecallStatus.OPEN);

    if (request.supplierId() != null) {
      Supplier supplier = supplierRepository.findById(request.supplierId())
          .orElseThrow(() -> new IllegalArgumentException(
              "Supplier not found with id: " + request.supplierId()));
      recallCase.setSupplier(supplier);
    }

    RecallCase saved = recallCaseRepository.save(recallCase);
    log.info("Recall case created: {} (id={})", saved.getTitle(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves a recall case by its ID.
   *
   * @param id the recall case identifier
   * @return the recall case response
   */
  public RecallCaseResponse getRecall(Long id) {
    RecallCase recallCase = recallCaseRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "Recall case not found with id: " + id));
    return mapToResponse(recallCase);
  }

  /**
   * Lists all recall cases for an organization, ordered by opened date descending.
   *
   * @param organizationId the organization identifier
   * @return list of recall case responses
   */
  public List<RecallCaseResponse> listRecalls(Long organizationId) {
    return recallCaseRepository.findByOrganizationIdOrderByOpenedAtDesc(organizationId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Maps a recall case entity to its response DTO.
   *
   * @param recallCase the recall case entity
   * @return the recall case response DTO
   */
  private RecallCaseResponse mapToResponse(RecallCase recallCase) {
    return new RecallCaseResponse(
        recallCase.getId(),
        recallCase.getTitle(),
        recallCase.getSupplier() != null ? recallCase.getSupplier().getId() : null,
        recallCase.getSupplier() != null ? recallCase.getSupplier().getName() : null,
        recallCase.getProductName(),
        recallCase.getBatchLot(),
        recallCase.getDescription(),
        recallCase.getOpenedAt(),
        recallCase.getClosedAt(),
        recallCase.getStatus().name()
    );
  }
}
