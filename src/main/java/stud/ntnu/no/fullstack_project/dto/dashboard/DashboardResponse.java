package stud.ntnu.no.fullstack_project.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload containing aggregated dashboard statistics for the current user's organization.
 *
 * @param totalChecklistTemplates  total number of active checklist templates
 * @param checklistsCompletedToday number of checklists completed today
 * @param temperatureAlertsToday   number of temperature warnings and critical alerts today
 * @param openDeviations           number of deviations with OPEN status
 * @param inProgressDeviations     number of deviations with IN_PROGRESS status
 * @param unreadNotifications      number of unread notifications for the current user
 */
@Schema(description = "Aggregated dashboard statistics for the current user's organization.")
public record DashboardResponse(
    @Schema(description = "Total number of active checklist templates.", example = "5")
    long totalChecklistTemplates,

    @Schema(description = "Number of checklists completed today.", example = "3")
    long checklistsCompletedToday,

    @Schema(description = "Number of temperature warnings and critical alerts today.", example = "1")
    long temperatureAlertsToday,

    @Schema(description = "Number of deviations with OPEN status.", example = "2")
    long openDeviations,

    @Schema(description = "Number of deviations with IN_PROGRESS status.", example = "1")
    long inProgressDeviations,

    @Schema(description = "Number of unread notifications for the current user.", example = "4")
    long unreadNotifications
) {}
