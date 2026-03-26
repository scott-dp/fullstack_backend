package stud.ntnu.no.fullstack_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.no.fullstack_project.dto.allergen.CreateIngredientRequest;
import stud.ntnu.no.fullstack_project.dto.allergen.IngredientResponse;
import stud.ntnu.no.fullstack_project.dto.allergen.UpdateIngredientRequest;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.service.IngredientService;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ingredients", description = "Endpoints for ingredient management")
public class IngredientController {

  private final IngredientService ingredientService;

  @GetMapping
  @Operation(summary = "List ingredients for current organization")
  public ResponseEntity<List<IngredientResponse>> listIngredients(
      @AuthenticationPrincipal AppUser currentUser) {
    return ResponseEntity.ok(
        ingredientService.listIngredients(currentUser.getOrganization().getId()));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Create a new ingredient")
  public ResponseEntity<IngredientResponse> createIngredient(
      @Valid @RequestBody CreateIngredientRequest request,
      @AuthenticationPrincipal AppUser currentUser) {
    log.info("Creating ingredient name={}", request.name());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ingredientService.createIngredient(request, currentUser));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an ingredient by ID")
  public ResponseEntity<IngredientResponse> getIngredient(@PathVariable Long id) {
    return ResponseEntity.ok(ingredientService.getIngredient(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Update an ingredient")
  public ResponseEntity<IngredientResponse> updateIngredient(
      @PathVariable Long id,
      @Valid @RequestBody UpdateIngredientRequest request) {
    log.info("Updating ingredient id={}", id);
    return ResponseEntity.ok(ingredientService.updateIngredient(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(summary = "Delete an ingredient")
  public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
    log.info("Deleting ingredient id={}", id);
    ingredientService.deleteIngredient(id);
    return ResponseEntity.noContent().build();
  }
}
