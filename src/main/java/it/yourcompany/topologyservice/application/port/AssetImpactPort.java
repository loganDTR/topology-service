package it.yourcompany.topologyservice.application.port;

import it.yourcompany.topologyservice.domain.result.ImpactSummaryResult;

import java.util.Optional;

/**
 * Output port for asset-impact graph traversal.
 *
 * <p>Owned by the application layer; implemented by the infrastructure layer.
 * {@link it.yourcompany.topologyservice.application.service.AssetImpactService}
 * depends on this interface, never on a concrete adapter.
 *
 * <p>Any change to the underlying graph technology (Neo4j, in-memory stub,
 * test double) requires only a new implementation of this interface — the
 * application service remains untouched and independently testable.
 */
public interface AssetImpactPort {

    /**
     * Returns the impact summary for the given asset identifier,
     * or {@link Optional#empty()} when no asset with that id exists in the graph.
     *
     * @param assetId the stable domain identifier of the asset
     */
    Optional<ImpactSummaryResult> findImpactSummary(String assetId);
}

