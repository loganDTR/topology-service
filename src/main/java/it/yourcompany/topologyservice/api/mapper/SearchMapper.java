package it.yourcompany.topologyservice.api.mapper;

import it.yourcompany.topologyservice.api.dto.NodeRefDto;
import it.yourcompany.topologyservice.api.dto.SearchResponse;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import it.yourcompany.topologyservice.domain.result.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps {@link SearchResult} domain objects to the {@link SearchResponse} API DTO. */
@Component
public class SearchMapper {

    public SearchResponse toResponse(String query, SearchResult result) {
        return new SearchResponse(
                query,
                toDtoList(result.assets()),
                toDtoList(result.services()),
                toDtoList(result.businessFunctions()),
                toDtoList(result.applications()),
                toDtoList(result.userJourneys()),
                toDtoList(result.teams()));
    }

    private NodeRefDto toDto(NodeRef ref) {
        return new NodeRefDto(ref.id(), ref.name());
    }

    private List<NodeRefDto> toDtoList(List<NodeRef> refs) {
        return refs.stream().map(this::toDto).toList();
    }
}

