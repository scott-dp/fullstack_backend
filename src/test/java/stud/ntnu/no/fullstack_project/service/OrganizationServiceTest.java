package stud.ntnu.no.fullstack_project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.no.fullstack_project.dto.organization.CreateOrganizationRequest;
import stud.ntnu.no.fullstack_project.dto.organization.OrganizationResponse;
import stud.ntnu.no.fullstack_project.entity.organization.Organization;
import stud.ntnu.no.fullstack_project.entity.organization.OrganizationType;
import stud.ntnu.no.fullstack_project.repository.organization.OrganizationRepository;
import stud.ntnu.no.fullstack_project.service.organization.OrganizationService;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationRepository organizationRepository;

  @InjectMocks
  private OrganizationService organizationService;

  // --- Helper methods ---

  private Organization buildOrg(Long id, String name, String orgNumber) {
    Organization org = new Organization();
    org.setId(id);
    org.setName(name);
    org.setOrganizationNumber(orgNumber);
    org.setAddress("Test Street 1");
    org.setPhone("12345678");
    org.setType(OrganizationType.RESTAURANT);
    org.setCreatedAt(LocalDateTime.now());
    return org;
  }

  // --- createOrganization tests ---

  @Test
  void createOrganization_validInput_createsOrg() {
    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "New Restaurant", "123456789", "Main Street 10", "99887766", "RESTAURANT"
    );

    when(organizationRepository.existsByOrganizationNumber("123456789")).thenReturn(false);
    when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
      Organization saved = invocation.getArgument(0);
      saved.setId(1L);
      saved.setCreatedAt(LocalDateTime.now());
      return saved;
    });

    OrganizationResponse response = organizationService.createOrganization(request);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("New Restaurant", response.name());
    assertEquals("123456789", response.organizationNumber());
    assertEquals("Main Street 10", response.address());
    assertEquals("99887766", response.phone());
    assertEquals("RESTAURANT", response.type());
    verify(organizationRepository).save(any(Organization.class));
  }

  @Test
  void createOrganization_duplicateOrgNumber_throws() {
    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Duplicate", "123456789", "Addr", "111", "BAR"
    );

    when(organizationRepository.existsByOrganizationNumber("123456789")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> organizationService.createOrganization(request));
    assertTrue(ex.getMessage().contains("already registered"));
    verify(organizationRepository, never()).save(any());
  }

  @Test
  void createOrganization_nullOrgNumber_skipsUniquenessCheck() {
    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "No OrgNum", null, "Addr", "111", "CAFE"
    );

    when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
      Organization saved = invocation.getArgument(0);
      saved.setId(2L);
      saved.setCreatedAt(LocalDateTime.now());
      return saved;
    });

    OrganizationResponse response = organizationService.createOrganization(request);

    assertNotNull(response);
    verify(organizationRepository, never()).existsByOrganizationNumber(anyString());
  }

  @Test
  void createOrganization_invalidType_throws() {
    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Bad Type", "999", "Addr", "111", "INVALID_TYPE"
    );

    when(organizationRepository.existsByOrganizationNumber("999")).thenReturn(false);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> organizationService.createOrganization(request));
    assertTrue(ex.getMessage().contains("Invalid organization type"));
  }

  // --- getOrganization tests ---

  @Test
  void getOrganization_existing_returnsResponse() {
    Organization org = buildOrg(1L, "Test Org", "111222333");
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

    OrganizationResponse response = organizationService.getOrganization(1L);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Test Org", response.name());
    assertEquals("111222333", response.organizationNumber());
  }

  @Test
  void getOrganization_nonExistent_throws() {
    when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> organizationService.getOrganization(999L));
    assertTrue(ex.getMessage().contains("not found"));
  }

  // --- listOrganizations tests ---

  @Test
  void listOrganizations_returnsAll() {
    Organization org1 = buildOrg(1L, "Org 1", "111");
    Organization org2 = buildOrg(2L, "Org 2", "222");
    when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));

    List<OrganizationResponse> result = organizationService.listOrganizations();

    assertEquals(2, result.size());
    assertEquals("Org 1", result.get(0).name());
    assertEquals("Org 2", result.get(1).name());
  }

  @Test
  void listOrganizations_emptyList_returnsEmpty() {
    when(organizationRepository.findAll()).thenReturn(List.of());

    List<OrganizationResponse> result = organizationService.listOrganizations();

    assertTrue(result.isEmpty());
  }

  // --- updateOrganization tests ---

  @Test
  void updateOrganization_validUpdate_works() {
    Organization existing = buildOrg(1L, "Old Name", "111222333");
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Updated Name", "111222333", "New Address", "55544433", "BAR"
    );

    OrganizationResponse response = organizationService.updateOrganization(1L, request);

    assertEquals("Updated Name", response.name());
    assertEquals("New Address", response.address());
    assertEquals("BAR", response.type());
    verify(organizationRepository).save(any(Organization.class));
  }

  @Test
  void updateOrganization_changingToExistingOrgNumber_throws() {
    Organization existing = buildOrg(1L, "Org", "111");
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(organizationRepository.existsByOrganizationNumber("222")).thenReturn(true);

    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Org", "222", "Addr", "111", "RESTAURANT"
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> organizationService.updateOrganization(1L, request));
    assertTrue(ex.getMessage().contains("already registered"));
  }

  @Test
  void updateOrganization_keepingSameOrgNumber_doesNotThrow() {
    Organization existing = buildOrg(1L, "Org", "111222333");
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Updated", "111222333", "New Addr", "999", "HOTEL"
    );

    OrganizationResponse response = organizationService.updateOrganization(1L, request);

    assertEquals("Updated", response.name());
    verify(organizationRepository, never()).existsByOrganizationNumber(anyString());
  }

  @Test
  void updateOrganization_nonExistentId_throws() {
    when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

    CreateOrganizationRequest request = new CreateOrganizationRequest(
        "Name", "123", "Addr", "111", "RESTAURANT"
    );

    assertThrows(IllegalArgumentException.class,
        () -> organizationService.updateOrganization(999L, request));
  }
}
