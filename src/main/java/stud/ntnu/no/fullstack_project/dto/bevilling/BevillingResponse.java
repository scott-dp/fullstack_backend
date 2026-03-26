package stud.ntnu.no.fullstack_project.dto.bevilling;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response payload representing a bevilling with its conditions and serving hours.
 */
@Schema(description = "Response representing a bevilling record with conditions and serving hours.")
public record BevillingResponse(
    @Schema(description = "Unique bevilling identifier.", example = "1")
    Long id,

    @Schema(description = "Municipality that issued the license.", example = "Trondheim")
    String municipality,

    @Schema(description = "Type of bevilling.", example = "SKJENKING")
    String bevillingType,

    @Schema(description = "Start date of the license.", example = "2025-01-01")
    LocalDate validFrom,

    @Schema(description = "End date of the license, or null.", example = "2027-12-31")
    LocalDate validTo,

    @Schema(description = "License number.", example = "SK-2025-001")
    String licenseNumber,

    @Schema(description = "Current license status.", example = "ACTIVE")
    String status,

    @Schema(description = "Alcohol groups allowed.")
    Set<String> alcoholGroupsAllowed,

    @Schema(description = "Serving area description.")
    String servingAreaDescription,

    @Schema(description = "Whether indoor serving is allowed.")
    boolean indoorAllowed,

    @Schema(description = "Whether outdoor serving is allowed.")
    boolean outdoorAllowed,

    @Schema(description = "Name of the styrer (license manager).")
    String styrerName,

    @Schema(description = "Name of the stedfortreder (deputy).")
    String stedfortrederName,

    @Schema(description = "Additional notes.")
    String notes,

    @Schema(description = "Attachment ID, if any.")
    Long attachmentId,

    @Schema(description = "Conditions attached to this bevilling.")
    List<ConditionResponse> conditions,

    @Schema(description = "Serving hours for this bevilling.")
    List<ServingHoursResponse> servingHours,

    @Schema(description = "Creation timestamp.")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp.")
    LocalDateTime updatedAt
) {}
