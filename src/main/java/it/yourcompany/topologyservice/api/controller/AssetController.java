package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.AssetImpactSummaryResponse;
import it.yourcompany.topologyservice.api.mapper.ImpactSummaryMapper;
import it.yourcompany.topologyservice.application.service.AssetImpactService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public AssetController(AssetImpactService assetImpactService,
                           ImpactSummaryMapper impactSummaryMapper) {
        this.assetImpactService = assetImpactService;
        this.impactSummaryMapper = impactSummaryMapper;
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
}

