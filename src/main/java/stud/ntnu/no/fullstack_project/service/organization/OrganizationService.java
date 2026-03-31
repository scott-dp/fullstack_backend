package stud.ntnu.no.fullstack_project.service.organization;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.dto.organization.OrganizationResponse;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;

/**
 * Service for organization read operations used by the superadmin area.
 *
 * <p>Currently only listing is exposed through the application API.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

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
