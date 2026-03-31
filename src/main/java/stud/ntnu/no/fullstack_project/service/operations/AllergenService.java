package stud.ntnu.no.fullstack_project.service.operations;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stud.ntnu.no.fullstack_project.dto.allergen.AllergenResponse;
import stud.ntnu.no.fullstack_project.entity.food.Allergen;
import stud.ntnu.no.fullstack_project.repository.food.AllergenRepository;

/**
 * Service for managing allergen reference data.
 *
 * <p>The 14 EU allergens are seeded at application startup. This service
 * provides read-only access to the active allergen list.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllergenService {

  private final AllergenRepository allergenRepository;

  /**
   * Returns all active allergens.
   *
   * @return list of active allergen responses
   */
  public List<AllergenResponse> listAllergens() {
    return allergenRepository.findByActiveTrue().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Maps an allergen entity to its response DTO.
   *
   * @param allergen the allergen entity
   * @return the allergen response DTO
   */
  AllergenResponse mapToResponse(Allergen allergen) {
    return new AllergenResponse(
        allergen.getId(),
        allergen.getCode(),
        allergen.getNameNo(),
        allergen.getNameEn()
    );
  }
}
