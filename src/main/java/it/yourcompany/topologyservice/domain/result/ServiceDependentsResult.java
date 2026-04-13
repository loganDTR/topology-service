package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/**
 * Downstream dependents of a given service plus derived business impact:
 * business functions, user journeys, and applications that depend on those services.
 */
public record ServiceDependentsResult(
        NodeRef service,
        int depth,
        List<NodeRef> dependentServices,
        List<NodeRef> impactedBusinessFunctions,
        List<NodeRef> impactedJourneys,
        List<NodeRef> impactedApplications
) {}

