package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/**
 * Domain model pairing an application with the subset of user journeys
 * it exposes that are impacted by a given asset or service outage.
 */
public record ApplicationImpact(
        NodeRef application,
        List<NodeRef> impactedJourneys
) {}

