package stud.ntnu.no.fullstack_project.dto.checklist;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating or updating a checklist template.
 *
 * <p>A template defines the structure of a checklist including its items,
 * frequency, and compliance category.</p>
 *
 * @param title       display title of the checklist template
 * @param description optional longer description
 * @param frequency   how often the checklist should be completed (e.g. DAILY)
 * @param category    compliance category (e.g. FOOD_SAFETY)
 * @param items       ordered list of checklist items
 */
@Schema(description = "Request payload for creating or updating a checklist template.")
public record CreateChecklistTemplateRequest(
    @Schema(description = "Display title of the checklist template.", example = "Morning Kitchen Checklist")
    @NotBlank @Size(max = 255)
    String title,

    @Schema(description = "Optional longer description of the checklist.", example = "Daily morning kitchen opening procedures.")
    @Size(max = 1000)
    String description,

    @Schema(description = "How often the checklist should be completed.", example = "DAILY")
    @NotNull
    String frequency,

    @Schema(description = "Compliance category the checklist belongs to.", example = "FOOD_SAFETY")
    @NotNull
    String category,

    @ArraySchema(schema = @Schema(implementation = CreateChecklistItemRequest.class))
    @NotEmpty @Valid
    List<CreateChecklistItemRequest> items
) {}
