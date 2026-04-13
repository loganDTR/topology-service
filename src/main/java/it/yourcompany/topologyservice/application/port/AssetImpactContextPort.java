package it.yourcompany.topologyservice.application.port;

import it.yourcompany.topologyservice.domain.result.AssetImpactContextResult;

import java.util.Optional;

/**
 * Output port for the richer asset-impact-context traversal.
 * Returns service, business-function, journey, and application impact.
 */
public interface AssetImpactContextPort {

    /**
     * Returns the full impact context for the given asset,
     * or {@link Optional#empty()} when no asset with that id exists.
     */
    Optional<AssetImpactContextResult> findImpactContext(String assetId);
}

