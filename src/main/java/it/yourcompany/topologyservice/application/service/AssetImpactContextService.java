package it.yourcompany.topologyservice.application.service;

import it.yourcompany.topologyservice.application.port.AssetImpactContextPort;
import it.yourcompany.topologyservice.domain.result.AssetImpactContextResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orchestrates the asset-impact-context use case.
 * Depends on {@link AssetImpactContextPort}; no Neo4j knowledge here.
 */
@Service
public class AssetImpactContextService {

    private final AssetImpactContextPort port;

    public AssetImpactContextService(AssetImpactContextPort port) {
        this.port = port;
    }

    @Transactional(readOnly = true)
    public Optional<AssetImpactContextResult> getImpactContext(String assetId) {
        return port.findImpactContext(assetId);
    }
}

