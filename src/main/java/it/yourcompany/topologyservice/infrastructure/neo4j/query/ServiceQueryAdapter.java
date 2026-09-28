package it.yourcompany.topologyservice.infrastructure.neo4j.query;

import it.yourcompany.topologyservice.application.port.ServiceQueryPort;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import it.yourcompany.topologyservice.domain.result.ServiceBusinessContextResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependenciesResult;
import it.yourcompany.topologyservice.domain.result.ServiceDependentsResult;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Graph traversal adapter for service-scoped queries.
 *
 * <p>All Cypher queries use {@code *1..5} as the maximum traversal bound for
 * variable-length paths; the requested {@code depth} (1–5) is applied as a
 * filter inside the query. Neo4j does not support dynamic bounds as parameters.
 */
@Component
public class ServiceQueryAdapter implements ServiceQueryPort {

    // language=Cypher
    private static final String DEPENDENCIES_CYPHER = """
            MATCH (s:Service {id: $serviceId})
            OPTIONAL MATCH path = (s)-[:DEPENDS_ON*1..5]->(dep:Service)
            WITH s, dep, min(length(path)) AS pathLen
            RETURN
              s.id   AS serviceId,
              s.name AS serviceName,
              collect(DISTINCT CASE WHEN dep IS NOT NULL AND pathLen <= $depth
                                   THEN {id: dep.id, name: dep.name} END) AS dependencies
            """;

    // language=Cypher
    private static final String DEPENDENTS_CYPHER = """
            MATCH (s:Service {id: $serviceId})
            OPTIONAL MATCH path = (dep:Service)-[:DEPENDS_ON*1..5]->(s)
            WITH s, dep, min(length(path)) AS pathLen
            WITH s, collect(DISTINCT CASE WHEN dep IS NOT NULL AND pathLen <= $depth
                                         THEN dep END) AS rawDeps
            WITH s, [n IN rawDeps WHERE n IS NOT NULL] AS depServices

            OPTIONAL MATCH (bf:BusinessFunction)-[:IMPLEMENTED_BY]->(svc)
              WHERE svc IN depServices
            WITH s, depServices, collect(DISTINCT bf) AS bfNodes

            OPTIONAL MATCH (uj:UserJourney)-[:REQUIRES]->(bf2)
              WHERE bf2 IN bfNodes
            WITH s, depServices, bfNodes, collect(DISTINCT uj) AS ujNodes

            OPTIONAL MATCH (app:Application)-[:EXPOSES]->(uj3)
              WHERE uj3 IN ujNodes
            WITH s, depServices, bfNodes, ujNodes, collect(DISTINCT app) AS appNodes

            RETURN
              s.id   AS serviceId,
              s.name AS serviceName,
              [svc IN depServices | {id: svc.id, name: svc.name}] AS dependentServices,
              [bn IN bfNodes      | {id: bn.id, name: bn.name}]   AS impactedBusinessFunctions,
              [un IN ujNodes      | {id: un.id, name: un.name}]   AS impactedJourneys,
              [an IN appNodes     | {id: an.id, name: an.name}]   AS impactedApplications
            """;

    // language=Cypher
    private static final String BUSINESS_CONTEXT_CYPHER = """
            MATCH (s:Service {id: $serviceId})

            OPTIONAL MATCH (bf:BusinessFunction)-[:IMPLEMENTED_BY]->(s)
            WITH s, collect(DISTINCT bf) AS bfNodes

            OPTIONAL MATCH (uj:UserJourney)-[:REQUIRES]->(bf2)
              WHERE bf2 IN bfNodes
            WITH s, bfNodes, collect(DISTINCT uj) AS ujNodes

            OPTIONAL MATCH (app:Application)-[:EXPOSES]->(uj3)
              WHERE uj3 IN ujNodes
            WITH s, bfNodes, ujNodes, collect(DISTINCT app) AS appNodes

            OPTIONAL MATCH (team:Team)-[:MANAGES]->(s)
            WITH s, bfNodes, ujNodes, appNodes, collect(DISTINCT team) AS teamNodes

            RETURN
              s.id   AS serviceId,
              s.name AS serviceName,
              [bf  IN bfNodes   | {id: bf.id,   name: bf.name}]   AS businessFunctions,
              [uj  IN ujNodes   | {id: uj.id,   name: uj.name}]   AS userJourneys,
              [app IN appNodes  | {id: app.id,  name: app.name}]  AS applications,
              [t   IN teamNodes | {id: t.id,    name: t.name}]    AS managingTeams
            """;

    private final Neo4jClient neo4jClient;

    public ServiceQueryAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ServiceDependenciesResult> findDependencies(String serviceId, int depth) {
        return neo4jClient.query(DEPENDENCIES_CYPHER)
                .bind(serviceId).to("serviceId")
                .bind(depth).to("depth")
                .fetch()
                .one()
                .map(row -> new ServiceDependenciesResult(
                        nodeRef(row, "serviceId", "serviceName"),
                        depth,
                        toNodeRefs((List<Map<String, Object>>) row.get("dependencies"))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ServiceDependentsResult> findDependents(String serviceId, int depth) {
        return neo4jClient.query(DEPENDENTS_CYPHER)
                .bind(serviceId).to("serviceId")
                .bind(depth).to("depth")
                .fetch()
                .one()
                .map(row -> new ServiceDependentsResult(
                        nodeRef(row, "serviceId", "serviceName"),
                        depth,
                        toNodeRefs((List<Map<String, Object>>) row.get("dependentServices")),
                        toNodeRefs((List<Map<String, Object>>) row.get("impactedBusinessFunctions")),
                        toNodeRefs((List<Map<String, Object>>) row.get("impactedJourneys")),
                        toNodeRefs((List<Map<String, Object>>) row.get("impactedApplications"))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ServiceBusinessContextResult> findBusinessContext(String serviceId) {
        return neo4jClient.query(BUSINESS_CONTEXT_CYPHER)
                .bind(serviceId).to("serviceId")
                .fetch()
                .one()
                .map(row -> new ServiceBusinessContextResult(
                        nodeRef(row, "serviceId", "serviceName"),
                        toNodeRefs((List<Map<String, Object>>) row.get("businessFunctions")),
                        toNodeRefs((List<Map<String, Object>>) row.get("userJourneys")),
                        toNodeRefs((List<Map<String, Object>>) row.get("applications")),
                        toNodeRefs((List<Map<String, Object>>) row.get("managingTeams"))));
    }

    // -------------------------------------------------------------------------
    // Shared mapping helpers
    // -------------------------------------------------------------------------

    private NodeRef nodeRef(Map<String, Object> row, String idKey, String nameKey) {
        return new NodeRef((String) row.get(idKey), (String) row.get(nameKey));
    }

    private List<NodeRef> toNodeRefs(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(m -> m != null && m.get("id") != null)
                .map(m -> new NodeRef((String) m.get("id"), (String) m.get("name")))
                .toList();
    }
}

