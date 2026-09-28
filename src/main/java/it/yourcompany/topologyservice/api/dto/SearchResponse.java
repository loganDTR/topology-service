package it.yourcompany.topologyservice.api.dto;

import java.util.List;

/** Response for {@code GET /api/v1/search?query=...}. */
public record SearchResponse(
        String query,
        List<NodeRefDto> assets,
        List<NodeRefDto> services,
        List<NodeRefDto> businessFunctions,
        List<NodeRefDto> applications,
        List<NodeRefDto> userJourneys,
        List<NodeRefDto> teams
) {}

