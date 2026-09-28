package it.yourcompany.topologyservice.domain.result;

import java.util.List;

/** Upstream DEPENDS_ON dependencies of a given service up to a bounded depth. */
public record ServiceDependenciesResult(
        NodeRef service,
        int depth,
        List<NodeRef> dependencies
) {}

