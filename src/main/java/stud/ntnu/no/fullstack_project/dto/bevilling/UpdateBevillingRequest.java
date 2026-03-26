package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Request payload for updating an existing bevilling.
 * All fields are optional; only non-null values will be applied.
 */
@Schema(description = "Request payload for updating a bevilling.")
public record UpdateBevillingRequest(
    @Schema(description = "New municipality.", example = "Oslo")
    String municipality,

    @Schema(description = "New bevilling type.", example = "COMBINED")
    String bevillingType,

    @Schema(description = "New start date.", example = "2025-01-01")
    String validFrom,

    @Schema(description = "New end date.", example = "2028-12-31")
    String validTo,

    @Schema(description = "New license number.", example = "SK-2025-002")
    String licenseNumber,

    @Schema(description = "New status.", example = "SUSPENDED")
    String status,

    @Schema(description = "Alcohol groups allowed.")
    Set<String> alcoholGroupsAllowed,

    @Schema(description = "Serving area description.")
    @Size(max = 2000)
    String servingAreaDescription,

    @Schema(description = "Whether indoor serving is allowed.")
    Boolean indoorAllowed,

    @Schema(description = "Whether outdoor serving is allowed.")
    Boolean outdoorAllowed,

    @Schema(description = "Name of the styrer.")
    String styrerName,

    @Schema(description = "Name of the stedfortreder.")
    String stedfortrederName,

    @Schema(description = "Additional notes.")
    @Size(max = 2000)
    String notes
) {}
