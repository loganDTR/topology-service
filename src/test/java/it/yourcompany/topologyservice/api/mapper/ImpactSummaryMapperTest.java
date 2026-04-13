package it.yourcompany.topologyservice.api.mapper;

import it.yourcompany.topologyservice.api.dto.AssetImpactSummaryResponse;
import it.yourcompany.topologyservice.domain.result.ImpactSummaryResult;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test for {@link ImpactSummaryMapper}.
 * No Spring context required — just Java records and the mapper.
 */
class ImpactSummaryMapperTest {

    private final ImpactSummaryMapper mapper = new ImpactSummaryMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        var asset   = new NodeRef("asset-db-1", "postgres-prod");
        var service = new NodeRef("svc-payment", "payment-service");
        var bf      = new NodeRef("bf-bonifico", "Bonifico");
        var journey = new NodeRef("uj-bonifico", "Bonifico Journey");

        var result = new ImpactSummaryResult(asset, List.of(service), List.of(bf), List.of(journey));

        AssetImpactSummaryResponse response = mapper.toResponse(result);

        assertThat(response.asset().id()).isEqualTo("asset-db-1");
        assertThat(response.asset().name()).isEqualTo("postgres-prod");
        assertThat(response.impactedServices()).hasSize(1);
        assertThat(response.impactedServices().get(0).id()).isEqualTo("svc-payment");
        assertThat(response.impactedBusinessFunctions()).hasSize(1);
        assertThat(response.impactedBusinessFunctions().get(0).id()).isEqualTo("bf-bonifico");
        assertThat(response.impactedJourneys()).hasSize(1);
        assertThat(response.impactedJourneys().get(0).id()).isEqualTo("uj-bonifico");
    }

    @Test
    void toResponse_withEmptyLists_returnsEmptyCollections() {
        var result = new ImpactSummaryResult(
                new NodeRef("asset-1", "vm"), List.of(), List.of(), List.of());

        AssetImpactSummaryResponse response = mapper.toResponse(result);

        assertThat(response.impactedServices()).isEmpty();
        assertThat(response.impactedBusinessFunctions()).isEmpty();
        assertThat(response.impactedJourneys()).isEmpty();
    }
}

