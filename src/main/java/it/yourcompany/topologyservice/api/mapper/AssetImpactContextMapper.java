package it.yourcompany.topologyservice.api.mapper;

import it.yourcompany.topologyservice.api.dto.ApplicationImpactDto;
import it.yourcompany.topologyservice.api.dto.AssetImpactContextResponse;
import it.yourcompany.topologyservice.api.dto.NodeRefDto;
import it.yourcompany.topologyservice.domain.result.ApplicationImpact;
import it.yourcompany.topologyservice.domain.result.AssetImpactContextResult;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps {@link AssetImpactContextResult} domain objects to API response DTOs. */
@Component
public class AssetImpactContextMapper {

    public AssetImpactContextResponse toResponse(AssetImpactContextResult result) {
        return new AssetImpactContextResponse(
                toDto(result.asset()),
                toDtoList(result.impactedServices()),
                toDtoList(result.impactedBusinessFunctions()),
                toDtoList(result.impactedJourneys()),
                result.applicationImpact().stream().map(this::toAppImpactDto).toList());
    }

    private ApplicationImpactDto toAppImpactDto(ApplicationImpact ai) {
        return new ApplicationImpactDto(toDto(ai.application()), toDtoList(ai.impactedJourneys()));
    }

    private NodeRefDto toDto(NodeRef ref) {
        return new NodeRefDto(ref.id(), ref.name());
    }

    private List<NodeRefDto> toDtoList(List<NodeRef> refs) {
        return refs.stream().map(this::toDto).toList();
    }
}

