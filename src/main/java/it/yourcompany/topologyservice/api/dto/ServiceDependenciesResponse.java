package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Response for {@code GET /api/v1/services/{serviceId}/dependencies}. */
public record ServiceDependenciesResponse(
        NodeRefDto service,
        int depth,
        List<NodeRefDto> dependencies
) {}

