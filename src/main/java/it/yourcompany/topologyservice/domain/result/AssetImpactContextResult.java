package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/**
 * Richer asset-impact domain result that includes application-level impact grouping.
 * Extends the data provided by {@link ImpactSummaryResult} with
 * per-application journey impact.
 */
public record AssetImpactContextResult(
        NodeRef asset,
        List<NodeRef> impactedServices,
        List<NodeRef> impactedBusinessFunctions,
        List<NodeRef> impactedJourneys,
        List<ApplicationImpact> applicationImpact
) {}

