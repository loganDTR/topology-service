package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Response for {@code GET /api/v1/assets/{assetId}/impact-context}. */
public record AssetImpactContextResponse(
        NodeRefDto asset,
        List<NodeRefDto> impactedServices,
        List<NodeRefDto> impactedBusinessFunctions,
        List<NodeRefDto> impactedJourneys,
        List<ApplicationImpactDto> applicationImpact
) {}

