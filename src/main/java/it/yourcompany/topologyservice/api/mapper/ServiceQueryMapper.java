package it.yourcompany.topologyservice.api.mapper;

import it.yourcompany.topologyservice.api.dto.NodeRefDto;
import it.yourcompany.topologyservice.api.dto.ServiceBusinessContextResponse;
import it.yourcompany.topologyservice.api.dto.ServiceDependenciesResponse;
import it.yourcompany.topologyservice.api.dto.ServiceDependentsResponse;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import it.yourcompany.topologyservice.domain.result.ServiceBusinessContextResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependenciesResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependentsResult;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps service-query domain results to API response DTOs. */
@Component
public class ServiceQueryMapper {

    public ServiceDependenciesResponse toDependenciesResponse(ServiceDependenciesResult result) {
        return new ServiceDependenciesResponse(
                toDto(result.service()),
                result.depth(),
                toDtoList(result.dependencies()));
    }

    public ServiceDependentsResponse toDependentsResponse(ServiceDependentsResult result) {
        return new ServiceDependentsResponse(
                toDto(result.service()),
                result.depth(),
                toDtoList(result.dependentServices()),
                toDtoList(result.impactedBusinessFunctions()),
                toDtoList(result.impactedJourneys()),
                toDtoList(result.impactedApplications()));
    }

    public ServiceBusinessContextResponse toBusinessContextResponse(ServiceBusinessContextResult result) {
        return new ServiceBusinessContextResponse(
                toDto(result.service()),
                toDtoList(result.businessFunctions()),
                toDtoList(result.userJourneys()),
                toDtoList(result.applications()),
                toDtoList(result.managingTeams()));
    }

    private NodeRefDto toDto(NodeRef ref) {
        return new NodeRefDto(ref.id(), ref.name());
    }

    private List<NodeRefDto> toDtoList(List<NodeRef> refs) {
        return refs.stream().map(this::toDto).toList();
    }
}

