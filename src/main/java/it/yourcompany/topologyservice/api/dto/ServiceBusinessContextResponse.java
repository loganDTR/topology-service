package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Response for {@code GET /api/v1/services/{serviceId}/business-context}. */
public record ServiceBusinessContextResponse(
        NodeRefDto service,
        List<NodeRefDto> businessFunctions,
        List<NodeRefDto> userJourneys,
        List<NodeRefDto> applications,
        List<NodeRefDto> managingTeams
) {}

