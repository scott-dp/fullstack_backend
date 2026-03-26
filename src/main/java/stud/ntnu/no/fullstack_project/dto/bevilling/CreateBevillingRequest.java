package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Request payload for creating a new bevilling (alcohol license).
 */
@Schema(description = "Request payload for creating a new bevilling.")
public record CreateBevillingRequest(
    @Schema(description = "Municipality that issued the license.", example = "Trondheim")
    @NotBlank
    String municipality,

    @Schema(description = "Type of bevilling (SKJENKING, SALG, COMBINED).", example = "SKJENKING")
    @NotNull
    String bevillingType,

    @Schema(description = "Start date of the license (ISO-8601).", example = "2025-01-01")
    @NotNull
    String validFrom,

    @Schema(description = "End date of the license (ISO-8601), or null for indefinite.", example = "2027-12-31")
    String validTo,

    @Schema(description = "License number issued by the municipality.", example = "SK-2025-001")
    String licenseNumber,

    @Schema(description = "Alcohol groups allowed (GROUP_1, GROUP_2, GROUP_3).")
    Set<String> alcoholGroupsAllowed,

    @Schema(description = "Description of the serving area.", example = "Main dining room and outdoor terrace")
    @Size(max = 2000)
    String servingAreaDescription,

    @Schema(description = "Whether indoor serving is allowed.", example = "true")
    Boolean indoorAllowed,

    @Schema(description = "Whether outdoor serving is allowed.", example = "false")
    Boolean outdoorAllowed,

    @Schema(description = "Name of the styrer (license manager).", example = "Ola Nordmann")
    String styrerName,

    @Schema(description = "Name of the stedfortreder (deputy license manager).", example = "Kari Nordmann")
    String stedfortrederName,

    @Schema(description = "Additional notes.", example = "Renewed annually")
    @Size(max = 2000)
    String notes
) {}
