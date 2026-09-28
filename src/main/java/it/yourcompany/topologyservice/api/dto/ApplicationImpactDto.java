package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Application paired with the subset of user journeys it exposes that are impacted. */
public record ApplicationImpactDto(
        NodeRefDto application,
        List<NodeRefDto> impactedJourneys
) {}

