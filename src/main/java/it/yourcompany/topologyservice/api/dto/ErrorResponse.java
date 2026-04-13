package it.yourcompany.topologyservice.api.dto;

import java.time.Instant;

/**
 * Compact, deterministic error body returned for all non-2xx responses.
 *
 * <p>Designed for service-to-service and AI consumption: every field is
 * always present, names are explicit, and the payload is JSON-serialisable
 * without additional configuration.
 *
 * @param status    HTTP status code (redundant with the response header, but
 *                  useful for consumers that unwrap the body before inspecting headers)
 * @param error     machine-readable error token (e.g. {@code "validation_error"})
 * @param message   human-readable description; may contain field-level detail for 400s
 * @param timestamp UTC instant at which the error was generated
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {}

