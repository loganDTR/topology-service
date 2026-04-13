package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.AssetImpactContextResponse;
import it.yourcompany.topologyservice.api.dto.AssetImpactSummaryResponse;
import it.yourcompany.topologyservice.api.mapper.AssetImpactContextMapper;
import it.yourcompany.topologyservice.api.mapper.ImpactSummaryMapper;
import it.yourcompany.topologyservice.application.service.AssetImpactContextService;
import it.yourcompany.topologyservice.application.service.AssetImpactService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * REST controller for asset-scoped topology queries.
 *
 * <p>Deliberately thin: validates input, delegates to the application service,
 * maps the domain result to an API response, and handles HTTP status codes.
 * No business logic lives here.
 */
@Validated
@RestController
@RequestMapping(path = "/api/v1/assets", produces = MediaType.APPLICATION_JSON_VALUE)
public class AssetController {

    private final AssetImpactService assetImpactService;
    private final ImpactSummaryMapper impactSummaryMapper;
    private final AssetImpactContextService assetImpactContextService;
    private final AssetImpactContextMapper assetImpactContextMapper;

    public AssetController(AssetImpactService assetImpactService,
                           ImpactSummaryMapper impactSummaryMapper,
                           AssetImpactContextService assetImpactContextService,
                           AssetImpactContextMapper assetImpactContextMapper) {
        this.assetImpactService = assetImpactService;
        this.impactSummaryMapper = impactSummaryMapper;
        this.assetImpactContextService = assetImpactContextService;
        this.assetImpactContextMapper = assetImpactContextMapper;
    }

    /**
     * Returns the blast-radius summary for a given infrastructure asset.
     *
     * <p>Traverses the graph to find impacted services, business functions,
     * and user journeys reachable from the asset.
     *
     * @param assetId the stable domain identifier of the asset
     * @return 200 with impact summary, or 404 if the asset is unknown
     */
    @GetMapping("/{assetId}/impact-summary")
    public ResponseEntity<AssetImpactSummaryResponse> getImpactSummary(
            @PathVariable @NotBlank String assetId) {

        return assetImpactService.getImpactSummary(assetId)
                .map(impactSummaryMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Richer asset impact context including per-application journey grouping.
     * Returns 404 with a JSON body when the asset id is unknown.
     */
    @GetMapping("/{assetId}/impact-context")
    public AssetImpactContextResponse getImpactContext(
            @PathVariable @NotBlank String assetId) {
        return assetImpactContextService.getImpactContext(assetId)
                .map(assetImpactContextMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException(
                        "Asset not found: " + assetId));
    }
}
