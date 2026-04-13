package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/**
 * Domain result describing the blast-radius of a single infrastructure asset.
 *
 * <p>Returned by the infrastructure query adapter and consumed by the
 * application service; it must not cross the API boundary directly.
 *
 * @param asset                      the queried asset
 * @param impactedServices           services deployed on or using the asset
 * @param impactedBusinessFunctions  business functions implemented by those services
 * @param impactedJourneys           user journeys that require those business functions
 */
public record ImpactSummaryResult(
        NodeRef asset,
        List<NodeRef> impactedServices,
        List<NodeRef> impactedBusinessFunctions,
        List<NodeRef> impactedJourneys
) {}

