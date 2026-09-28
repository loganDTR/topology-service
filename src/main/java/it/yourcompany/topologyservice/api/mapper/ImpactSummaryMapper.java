package it.yourcompany.topologyservice.api.mapper;

import it.yourcompany.topologyservice.api.dto.AssetImpactSummaryResponse;
import it.yourcompany.topologyservice.api.dto.NodeRefDto;
import it.yourcompany.topologyservice.domain.result.ImpactSummaryResult;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts {@link ImpactSummaryResult} domain objects to
 * {@link AssetImpactSummaryResponse} API DTOs.
 *
 * <p>Kept as a Spring component so it can be injected and tested in isolation.
 */
@Component
public class ImpactSummaryMapper {

    public AssetImpactSummaryResponse toResponse(ImpactSummaryResult result) {
        return new AssetImpactSummaryResponse(
                toDto(result.asset()),
                toDtoList(result.impactedServices()),
                toDtoList(result.impactedBusinessFunctions()),
                toDtoList(result.impactedJourneys())
        );
    }

    private NodeRefDto toDto(NodeRef ref) {
        return new NodeRefDto(ref.id(), ref.name());
    }

    private List<NodeRefDto> toDtoList(List<NodeRef> refs) {
        return refs.stream().map(this::toDto).toList();
    }
}

