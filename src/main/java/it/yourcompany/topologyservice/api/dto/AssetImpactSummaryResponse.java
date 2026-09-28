package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/v1/assets/{assetId}/impact-summary}.
 *
 * <p>Structured for downstream AI consumption: deterministic field names,
 * stable identifiers, no raw graph internals.
 *
 * @param asset                      the queried asset
 * @param impactedServices           services directly using or deployed on the asset
 * @param impactedBusinessFunctions  business functions implemented by those services
 * @param impactedJourneys           user journeys that require those business functions
 */
public record AssetImpactSummaryResponse(
        NodeRefDto asset,
        List<NodeRefDto> impactedServices,
        List<NodeRefDto> impactedBusinessFunctions,
        List<NodeRefDto> impactedJourneys
) {}

