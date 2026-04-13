package it.yourcompany.topologyservice.domain.result;

/**
 * Lightweight reference to any labelled graph node.
 * Carries only the stable {@code id} and human-readable {@code name}
 * — never raw Neo4j internal identifiers.
 */
public record NodeRef(String id, String name) {}

