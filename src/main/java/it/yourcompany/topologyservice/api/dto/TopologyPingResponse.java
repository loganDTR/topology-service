package it.yourcompany.topologyservice.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Response payload for the topology ping endpoint.
 *
 * @param service   logical name of this service
 * @param status    current operational status (e.g. "UP")
 * @param timestamp UTC instant at which the response was produced
 */
public record TopologyPingResponse(
        String service,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {}

