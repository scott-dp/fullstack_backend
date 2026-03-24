package stud.ntnu.no.fullstack_project.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.no.fullstack_project.dto.organization.CreateOrganizationRequest;
import stud.ntnu.no.fullstack_project.dto.organization.OrganizationResponse;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

/**
 * Service for managing organizations.
 *
 * <p>Provides business logic for creating, retrieving, listing, and updating
 * organization records.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  /**
   * Creates a new organization after validating uniqueness of the organization number.
   *
   * @param request the organization details
   * @return the created organization response
   */
  @Transactional
  public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
    if (request.organizationNumber() != null && !request.organizationNumber().isBlank()
        && organizationRepository.existsByOrganizationNumber(request.organizationNumber())) {
      throw new IllegalArgumentException("Organization number is already registered");
    }

    OrganizationType type;
    try {
      type = OrganizationType.valueOf(request.type());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid organization type: " + request.type());
    }

    Organization organization = new Organization();
    organization.setName(request.name());
    organization.setOrganizationNumber(request.organizationNumber());
    organization.setAddress(request.address());
    organization.setPhone(request.phone());
    organization.setType(type);

    Organization saved = organizationRepository.save(organization);
    log.info("Organization created: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Retrieves an organization by its ID.
   *
   * @param id the organization identifier
   * @return the organization response
   */
  public OrganizationResponse getOrganization(Long id) {
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));
    return mapToResponse(organization);
  }

  /**
   * Lists all registered organizations.
   *
   * @return list of organization responses
   */
  public List<OrganizationResponse> listOrganizations() {
    return organizationRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an existing organization's details.
   *
   * @param id      the organization identifier
   * @param request the updated organization details
   * @return the updated organization response
   */
  @Transactional
  public OrganizationResponse updateOrganization(Long id, CreateOrganizationRequest request) {
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));

    if (request.organizationNumber() != null && !request.organizationNumber().isBlank()
        && !request.organizationNumber().equals(organization.getOrganizationNumber())
        && organizationRepository.existsByOrganizationNumber(request.organizationNumber())) {
      throw new IllegalArgumentException("Organization number is already registered");
    }

    OrganizationType type;
    try {
      type = OrganizationType.valueOf(request.type());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid organization type: " + request.type());
    }

    organization.setName(request.name());
    organization.setOrganizationNumber(request.organizationNumber());
    organization.setAddress(request.address());
    organization.setPhone(request.phone());
    organization.setType(type);

    Organization saved = organizationRepository.save(organization);
    log.info("Organization updated: {} (id={})", saved.getName(), saved.getId());
    return mapToResponse(saved);
  }

  /**
   * Maps an organization entity to its response DTO.
   *
   * @param org the organization entity
   * @return the organization response DTO
   */
  private OrganizationResponse mapToResponse(Organization org) {
    return new OrganizationResponse(
        org.getId(),
        org.getName(),
        org.getOrganizationNumber(),
        org.getAddress(),
        org.getPhone(),
        org.getType().name(),
        org.getCreatedAt()
    );
  }
}
