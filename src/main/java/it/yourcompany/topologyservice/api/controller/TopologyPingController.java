package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.TopologyPingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Lightweight liveness probe for the topology service.
 * Does not touch Neo4j — use the Actuator health endpoint for that.
 */
@RestController
@RequestMapping(path = "/api/v1/ping", produces = MediaType.APPLICATION_JSON_VALUE)
public class TopologyPingController {

    private final String serviceName;

    public TopologyPingController(
            @Value("${spring.application.name:topology-service}") String serviceName) {
        this.serviceName = serviceName;
    }

    @GetMapping("/topology")
    public TopologyPingResponse ping() {
        return new TopologyPingResponse(serviceName, "UP", Instant.now());
    }
}

