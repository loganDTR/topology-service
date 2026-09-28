package it.yourcompany.topologyservice.application.port;

import it.yourcompany.topologyservice.domain.result.ServiceBusinessContextResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependenciesResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependentsResult;

import java.util.Optional;

/**
 * Output port for service-scoped topology queries.
 * Groups three related use cases that all start from a Service node.
 */
public interface ServiceQueryPort {

    /** Upstream services that the given service depends on, up to {@code depth} hops. */
    Optional<ServiceDependenciesResult> findDependencies(String serviceId, int depth);

    /** Downstream services that depend on the given service, plus derived business impact. */
    Optional<ServiceDependentsResult> findDependents(String serviceId, int depth);

    /** Business-oriented view: functions, journeys, applications, and owning teams. */
    Optional<ServiceBusinessContextResult> findBusinessContext(String serviceId);
}

