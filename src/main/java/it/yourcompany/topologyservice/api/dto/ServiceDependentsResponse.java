package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Response for {@code GET /api/v1/services/{serviceId}/dependents}. */
public record ServiceDependentsResponse(
        NodeRefDto service,
        int depth,
        List<NodeRefDto> dependentServices,
        List<NodeRefDto> impactedBusinessFunctions,
        List<NodeRefDto> impactedJourneys,
        List<NodeRefDto> impactedApplications
) {}

