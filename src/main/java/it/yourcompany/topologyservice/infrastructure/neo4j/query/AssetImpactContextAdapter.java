package it.yourcompany.topologyservice.infrastructure.neo4j.query;

import it.yourcompany.topologyservice.application.port.AssetImpactContextPort;
import it.yourcompany.topologyservice.domain.result.ApplicationImpact;
import it.yourcompany.topologyservice.domain.result.AssetImpactContextResult;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Graph traversal adapter for the asset-impact-context use case.
 *
 * <p>Runs two explicit Cypher queries:
 * <ol>
 *   <li>Main query — identical service/BF/journey traversal to impact-summary,
 *       plus returns journeyIds for the second query.</li>
 *   <li>Application impact query — groups applications by the impacted journeys
 *       they expose.</li>
 * </ol>
 */
@Component
public class AssetImpactContextAdapter implements AssetImpactContextPort {

    // language=Cypher
    private static final String IMPACT_CONTEXT_CYPHER = """
            MATCH (a:Asset {id: $assetId})

            OPTIONAL MATCH (s1:Service)-[:DEPLOYED_ON]->(a)
            WITH a, collect(DISTINCT s1) AS deployed

            OPTIONAL MATCH (s2:Service)-[:USES]->(a)
            WITH a, deployed, collect(DISTINCT s2) AS directUses

            OPTIONAL MATCH (s3:Service)-[:USES]->(:Database)-[:HOSTED_ON]->(a)
            WITH a, deployed, directUses, collect(DISTINCT s3) AS hostedUses

            WITH a, deployed + directUses + hostedUses AS combined

            UNWIND CASE WHEN size(combined) > 0 THEN combined ELSE [null] END AS svc
            WITH a, collect(DISTINCT svc) AS allServices
            WITH a, [s IN allServices WHERE s IS NOT NULL] AS serviceNodes

            OPTIONAL MATCH (bf:BusinessFunction)-[:IMPLEMENTED_BY]->(s)
              WHERE s IN serviceNodes
            WITH a, serviceNodes, collect(DISTINCT bf) AS bfNodes

            OPTIONAL MATCH (uj:UserJourney)-[:REQUIRES]->(bf)
              WHERE bf IN bfNodes
            WITH a, serviceNodes, bfNodes, collect(DISTINCT uj) AS ujNodes

            RETURN
              a.id   AS assetId,
              a.name AS assetName,
              [sn IN serviceNodes | {id: sn.id, name: sn.name}] AS impactedServices,
              [bn IN bfNodes      | {id: bn.id, name: bn.name}] AS impactedBusinessFunctions,
              [un IN ujNodes      | {id: un.id, name: un.name}] AS impactedJourneys,
              [un IN ujNodes      | un.id]                      AS journeyIds
            """;

    // language=Cypher
    private static final String APP_IMPACT_CYPHER = """
            UNWIND $journeyIds AS jId
            MATCH (uj:UserJourney {id: jId})
            MATCH (app:Application)-[:EXPOSES]->(uj)
            WITH app, collect(DISTINCT {id: uj.id, name: uj.name}) AS journeys
            RETURN app.id AS appId, app.name AS appName, journeys
            ORDER BY app.id
            """;

    private final Neo4jClient neo4jClient;

    public AssetImpactContextAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<AssetImpactContextResult> findImpactContext(String assetId) {
        return neo4jClient.query(IMPACT_CONTEXT_CYPHER)
                .bind(assetId).to("assetId")
                .fetch()
                .one()
                .map(row -> {
                    NodeRef asset = new NodeRef(
                            (String) row.get("assetId"),
                            (String) row.get("assetName"));
                    List<NodeRef> services =
                            toNodeRefs((List<Map<String, Object>>) row.get("impactedServices"));
                    List<NodeRef> bfs =
                            toNodeRefs((List<Map<String, Object>>) row.get("impactedBusinessFunctions"));
                    List<NodeRef> journeys =
                            toNodeRefs((List<Map<String, Object>>) row.get("impactedJourneys"));
                    List<String> journeyIds = (List<String>) row.get("journeyIds");

                    List<ApplicationImpact> appImpact =
                            (journeyIds == null || journeyIds.isEmpty())
                                    ? List.of()
                                    : findApplicationImpact(journeyIds);

                    return new AssetImpactContextResult(asset, services, bfs, journeys, appImpact);
                });
    }

    @SuppressWarnings("unchecked")
    private List<ApplicationImpact> findApplicationImpact(List<String> journeyIds) {
        return neo4jClient.query(APP_IMPACT_CYPHER)
                .bind(journeyIds).to("journeyIds")
                .fetch()
                .all()
                .stream()
                .map(row -> new ApplicationImpact(
                        new NodeRef((String) row.get("appId"), (String) row.get("appName")),
                        toNodeRefs((List<Map<String, Object>>) row.get("journeys"))))
                .toList();
    }

    private List<NodeRef> toNodeRefs(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(m -> m != null && m.get("id") != null)
                .map(m -> new NodeRef((String) m.get("id"), (String) m.get("name")))
                .toList();
    }
}

