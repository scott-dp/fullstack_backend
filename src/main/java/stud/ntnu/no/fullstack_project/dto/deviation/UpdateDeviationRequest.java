package stud.ntnu.no.fullstack_project.dto.deviation;

public record UpdateDeviationRequest(
    String status,
    Long assignedToId
) {}
