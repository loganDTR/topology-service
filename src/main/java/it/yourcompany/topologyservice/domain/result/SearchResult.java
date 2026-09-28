package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/** Entity search result, grouped by label type. Each list is bounded and deduplicated. */
public record SearchResult(
        List<NodeRef> assets,
        List<NodeRef> services,
        List<NodeRef> businessFunctions,
        List<NodeRef> applications,
        List<NodeRef> userJourneys,
        List<NodeRef> teams
) {}

