package stud.ntnu.no.fullstack_project.dto.dashboard;

public record DashboardResponse(
    long totalChecklistTemplates,
    long checklistsCompletedToday,
    long temperatureAlertsToday,
    long openDeviations,
    long inProgressDeviations,
    long unreadNotifications
) {}
