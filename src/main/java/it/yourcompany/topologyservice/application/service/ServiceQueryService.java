package it.yourcompany.topologyservice.application.service;

import it.yourcompany.topologyservice.application.port.ServiceQueryPort;
import it.yourcompany.topologyservice.domain.result.ServiceBusinessContextResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependenciesResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependentsResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orchestrates service-scoped topology queries.
 * Depends on {@link ServiceQueryPort}; no Neo4j knowledge here.
 */
@Service
public class ServiceQueryService {

    private final ServiceQueryPort port;

    public ServiceQueryService(ServiceQueryPort port) {
        this.port = port;
    }

    @Transactional(readOnly = true)
    public Optional<ServiceDependenciesResult> getDependencies(String serviceId, int depth) {
        return port.findDependencies(serviceId, depth);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceDependentsResult> getDependents(String serviceId, int depth) {
        return port.findDependents(serviceId, depth);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceBusinessContextResult> getBusinessContext(String serviceId) {
        return port.findBusinessContext(serviceId);
    }
}

