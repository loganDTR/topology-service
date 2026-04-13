package it.yourcompany.topologyservice.infrastructure.neo4j.query;

import it.yourcompany.topologyservice.application.port.AssetImpactPort;
import it.yourcompany.topologyservice.domain.result.ImpactSummaryResult;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Custom graph traversal adapter for asset-impact queries.
 *
 * <p>Uses {@link Neo4jClient} directly so that the Cypher remains explicit
 * and results are mapped to domain types before leaving this class.
 * No raw Neo4j objects are returned.
 */
@Component
public class ImpactQueryAdapter implements AssetImpactPort {

    // language=Cypher
    private static final String IMPACT_SUMMARY_CYPHER = """
            MATCH (a:Asset {id: $assetId})

            // Path 1: services directly deployed on this asset (e.g. AKS cluster)
            OPTIONAL MATCH (s1:Service)-[:DEPLOYED_ON]->(a)
            WITH a, collect(DISTINCT s1) AS deployed

            // Path 2: services that directly use this asset (e.g. a Database node)
            OPTIONAL MATCH (s2:Service)-[:USES]->(a)
            WITH a, deployed, collect(DISTINCT s2) AS directUses

            // Path 3: services that use a Database which is hosted on this asset
            OPTIONAL MATCH (s3:Service)-[:USES]->(:Database)-[:HOSTED_ON]->(a)
            WITH a, deployed, directUses, collect(DISTINCT s3) AS hostedUses

            // Pure list concatenation — no aggregation in this step
            WITH a, deployed + directUses + hostedUses AS combined

            // Deduplicate across all three paths
            UNWIND CASE WHEN size(combined) > 0 THEN combined ELSE [null] END AS svc
            WITH a, collect(DISTINCT svc) AS allServices
            WITH a, [s IN allServices WHERE s IS NOT NULL] AS serviceNodes

            // Traverse: services -> business functions they implement
            OPTIONAL MATCH (bf:BusinessFunction)-[:IMPLEMENTED_BY]->(s)
              WHERE s IN serviceNodes
            WITH a, serviceNodes, collect(DISTINCT bf) AS bfNodes

            // Traverse: business functions -> user journeys that require them
            OPTIONAL MATCH (uj:UserJourney)-[:REQUIRES]->(bf)
              WHERE bf IN bfNodes

            RETURN
              a.id                 AS assetId,
              a.name               AS assetName,
              [sn IN serviceNodes  | {id: sn.id, name: sn.name}] AS impactedServices,
              [bn IN bfNodes       | {id: bn.id, name: bn.name}] AS impactedBusinessFunctions,
              collect(DISTINCT CASE WHEN uj IS NOT NULL
                                   THEN {id: uj.id, name: uj.name} END) AS impactedJourneys
            """;

    private final Neo4jClient neo4jClient;

    public ImpactQueryAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ImpactSummaryResult> findImpactSummary(String assetId) {
        return neo4jClient.query(IMPACT_SUMMARY_CYPHER)
                .bind(assetId).to("assetId")
                .fetch()
                .one()
                .map(this::toImpactSummaryResult);
    }

    // -------------------------------------------------------------------------
    // Private mapping helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private ImpactSummaryResult toImpactSummaryResult(Map<String, Object> row) {
        NodeRef asset = new NodeRef(
                (String) row.get("assetId"),
                (String) row.get("assetName")
        );
        return new ImpactSummaryResult(
                asset,
                toNodeRefs((List<Map<String, Object>>) row.get("impactedServices")),
                toNodeRefs((List<Map<String, Object>>) row.get("impactedBusinessFunctions")),
                toNodeRefs((List<Map<String, Object>>) row.get("impactedJourneys"))
        );
    }

    private List<NodeRef> toNodeRefs(List<Map<String, Object>> raw) {
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .filter(m -> m != null && m.get("id") != null)
                .map(m -> new NodeRef((String) m.get("id"), (String) m.get("name")))
                .toList();
    }
}

