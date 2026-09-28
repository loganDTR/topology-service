package it.yourcompany.topologyservice.application.service;

import it.yourcompany.topologyservice.application.port.AssetImpactPort;
import it.yourcompany.topologyservice.domain.result.ImpactSummaryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orchestrates asset-impact use cases.
 *
 * <p>This service owns the use-case boundary: it receives plain identifiers,
 * delegates graph traversal through {@link AssetImpactPort}, and returns domain
 * results. It has no knowledge of Neo4j or any other persistence technology.
 */
@Service
public class AssetImpactService {

    private final AssetImpactPort assetImpactPort;

    public AssetImpactService(AssetImpactPort assetImpactPort) {
        this.assetImpactPort = assetImpactPort;
    }

    /**
     * Returns the full impact summary for the given asset,
     * or {@link Optional#empty()} when the asset does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<ImpactSummaryResult> getImpactSummary(String assetId) {
        return assetImpactPort.findImpactSummary(assetId);
    }
}

