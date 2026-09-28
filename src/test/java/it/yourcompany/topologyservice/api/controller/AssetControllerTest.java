package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.AssetImpactSummaryResponse;
import it.yourcompany.topologyservice.api.dto.NodeRefDto;
import it.yourcompany.topologyservice.api.mapper.AssetImpactContextMapper;
import it.yourcompany.topologyservice.api.mapper.ImpactSummaryMapper;
import it.yourcompany.topologyservice.application.service.AssetImpactContextService;
import it.yourcompany.topologyservice.application.service.AssetImpactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AssetImpactService assetImpactService;

    @MockitoBean
    ImpactSummaryMapper impactSummaryMapper;

    @MockitoBean
    AssetImpactContextService assetImpactContextService;

    @MockitoBean
    AssetImpactContextMapper assetImpactContextMapper;

    // ------------------------------------------------------------------
    // Happy path — impact-summary returns 200 with correct body
    // ------------------------------------------------------------------

    @Test
    void getImpactSummary_whenAssetExists_returns200WithBody() throws Exception {
        var domainResult = new it.yourcompany.topologyservice.domain.result.ImpactSummaryResult(
                new it.yourcompany.topologyservice.domain.result.NodeRef("asset-1", "prod-vm"),
                List.of(new it.yourcompany.topologyservice.domain.result.NodeRef("svc-1", "auth-service")),
                List.of(),
                List.of());

        var apiResponse = new AssetImpactSummaryResponse(
                new NodeRefDto("asset-1", "prod-vm"),
                List.of(new NodeRefDto("svc-1", "auth-service")),
                List.of(),
                List.of());

        when(assetImpactService.getImpactSummary("asset-1")).thenReturn(Optional.of(domainResult));
        when(impactSummaryMapper.toResponse(domainResult)).thenReturn(apiResponse);

        mockMvc.perform(get("/api/v1/assets/asset-1/impact-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asset.id").value("asset-1"))
                .andExpect(jsonPath("$.asset.name").value("prod-vm"))
                .andExpect(jsonPath("$.impactedServices[0].id").value("svc-1"))
                .andExpect(jsonPath("$.impactedBusinessFunctions").isArray())
                .andExpect(jsonPath("$.impactedJourneys").isArray());
    }

    // ------------------------------------------------------------------
    // Not found — impact-summary returns 404 when asset is unknown
    // ------------------------------------------------------------------

    @Test
    void getImpactSummary_whenAssetNotFound_returns404() throws Exception {
        when(assetImpactService.getImpactSummary("unknown-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/assets/unknown-id/impact-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // impact-context returns 404 with JSON body via GlobalExceptionHandler
    // ------------------------------------------------------------------

    @Test
    void getImpactContext_whenAssetNotFound_returns404WithJsonBody() throws Exception {
        when(assetImpactContextService.getImpactContext("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/assets/ghost/impact-context")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.path").value("/api/v1/assets/ghost/impact-context"));
    }
}




