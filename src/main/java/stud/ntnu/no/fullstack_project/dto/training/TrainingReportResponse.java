package stud.ntnu.no.fullstack_project.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload summarizing training statistics for an organization.
 *
 * @param totalTemplates   total number of training templates
 * @param totalAssignments total number of assignments
 * @param completedCount   number of completed assignments
 * @param overdueCount     number of overdue assignments
 * @param completionRate   completion rate as a percentage (0.0 - 100.0)
 */
@Schema(description = "Summary report of training statistics for an organization.")
public record TrainingReportResponse(
    @Schema(description = "Total number of training templates.", example = "10")
    long totalTemplates,

    @Schema(description = "Total number of assignments.", example = "50")
    long totalAssignments,

    @Schema(description = "Number of completed assignments.", example = "35")
    long completedCount,

    @Schema(description = "Number of overdue assignments.", example = "5")
    long overdueCount,

    @Schema(description = "Completion rate as a percentage.", example = "70.0")
    double completionRate
) {}
