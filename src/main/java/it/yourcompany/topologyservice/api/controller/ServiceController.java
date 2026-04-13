package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.ServiceBusinessContextResponse;
import it.yourcompany.topologyservice.api.dto.ServiceDependenciesResponse;
import it.yourcompany.topologyservice.api.dto.ServiceDependentsResponse;
import it.yourcompany.topologyservice.api.mapper.ServiceQueryMapper;
import it.yourcompany.topologyservice.application.service.ServiceQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * REST controller for service-scoped topology queries.
 * Thin layer: validates, delegates to {@link ServiceQueryService}, maps to DTOs.
 */
@Validated
@RestController
@RequestMapping(path = "/api/v1/services", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceController {

    private final ServiceQueryService serviceQueryService;
    private final ServiceQueryMapper serviceQueryMapper;

    public ServiceController(ServiceQueryService serviceQueryService,
                             ServiceQueryMapper serviceQueryMapper) {
        this.serviceQueryService = serviceQueryService;
        this.serviceQueryMapper = serviceQueryMapper;
    }

    /**
     * Returns upstream services that the given service depends on, up to {@code depth} hops.
     * Depth is validated to 1–5.
     */
    @GetMapping("/{serviceId}/dependencies")
    public ServiceDependenciesResponse getDependencies(
            @PathVariable @NotBlank String serviceId,
            @RequestParam(defaultValue = "2") @Min(1) @Max(5) int depth) {
        return serviceQueryService.getDependencies(serviceId, depth)
                .map(serviceQueryMapper::toDependenciesResponse)
                .orElseThrow(() -> new NoSuchElementException("Service not found: " + serviceId));
    }

    /**
     * Returns downstream services that depend on the given service,
     * plus derived business functions, user journeys, and applications.
     */
    @GetMapping("/{serviceId}/dependents")
    public ServiceDependentsResponse getDependents(
            @PathVariable @NotBlank String serviceId,
            @RequestParam(defaultValue = "2") @Min(1) @Max(5) int depth) {
        return serviceQueryService.getDependents(serviceId, depth)
                .map(serviceQueryMapper::toDependentsResponse)
                .orElseThrow(() -> new NoSuchElementException("Service not found: " + serviceId));
    }

    /**
     * Returns the business context of the given service: implemented business functions,
     * user journeys, applications, and owning teams.
     */
    @GetMapping("/{serviceId}/business-context")
    public ServiceBusinessContextResponse getBusinessContext(
            @PathVariable @NotBlank String serviceId) {
        return serviceQueryService.getBusinessContext(serviceId)
                .map(serviceQueryMapper::toBusinessContextResponse)
                .orElseThrow(() -> new NoSuchElementException("Service not found: " + serviceId));
    }
}

