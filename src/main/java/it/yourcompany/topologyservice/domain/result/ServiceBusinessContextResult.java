package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/**
 * Business-oriented view of a service: the business functions it implements,
 * the user journeys and applications built on top, and the teams that own it.
 */
public record ServiceBusinessContextResult(
        NodeRef service,
        List<NodeRef> businessFunctions,
        List<NodeRef> userJourneys,
        List<NodeRef> applications,
        List<NodeRef> managingTeams
) {}

