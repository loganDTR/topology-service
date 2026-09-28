package it.yourcompany.topologyservice.api.dto;

/**
 * API-facing reference to a graph node.
 * Exposes only the stable domain {@code id} and the human-readable {@code name}.
 */
public record NodeRefDto(String id, String name) {}

