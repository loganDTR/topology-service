package it.yourcompany.topologyservice.infrastructure.neo4j.query;

import it.yourcompany.topologyservice.application.port.SearchPort;
import it.yourcompany.topologyservice.domain.result.NodeRef;
import it.yourcompany.topologyservice.domain.result.SearchResult;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Graph traversal adapter for topology entity search.
 *
 * <p>Performs case-insensitive partial matching on {@code id} and {@code name}
 * across all main entity labels. Results per label are capped at 10 using
 * Cypher list slicing. The query does full-label scans; adding Neo4j full-text
 * indexes on {@code id} and {@code name} properties would significantly improve
 * performance at scale.
 */
@Component
public class SearchAdapter implements SearchPort {

    // language=Cypher
    private static final String SEARCH_CYPHER = """
            WITH toLower($query) AS q

            OPTIONAL MATCH (a:Asset)
              WHERE toLower(a.id) CONTAINS q OR toLower(a.name) CONTAINS q
            WITH q, collect(DISTINCT CASE WHEN a IS NOT NULL
                            THEN {id: a.id, name: a.name} END)[0..10] AS assets

            OPTIONAL MATCH (s:Service)
              WHERE toLower(s.id) CONTAINS q OR toLower(s.name) CONTAINS q
            WITH q, assets, collect(DISTINCT CASE WHEN s IS NOT NULL
                            THEN {id: s.id, name: s.name} END)[0..10] AS services

            OPTIONAL MATCH (bf:BusinessFunction)
              WHERE toLower(bf.id) CONTAINS q OR toLower(bf.name) CONTAINS q
            WITH q, assets, services, collect(DISTINCT CASE WHEN bf IS NOT NULL
                            THEN {id: bf.id, name: bf.name} END)[0..10] AS businessFunctions

            OPTIONAL MATCH (app:Application)
              WHERE toLower(app.id) CONTAINS q OR toLower(app.name) CONTAINS q
            WITH q, assets, services, businessFunctions,
                 collect(DISTINCT CASE WHEN app IS NOT NULL
                         THEN {id: app.id, name: app.name} END)[0..10] AS applications

            OPTIONAL MATCH (uj:UserJourney)
              WHERE toLower(uj.id) CONTAINS q OR toLower(uj.name) CONTAINS q
            WITH q, assets, services, businessFunctions, applications,
                 collect(DISTINCT CASE WHEN uj IS NOT NULL
                         THEN {id: uj.id, name: uj.name} END)[0..10] AS userJourneys

            OPTIONAL MATCH (t:Team)
              WHERE toLower(t.id) CONTAINS q OR toLower(t.name) CONTAINS q
            WITH q, assets, services, businessFunctions, applications, userJourneys,
                 collect(DISTINCT CASE WHEN t IS NOT NULL
                         THEN {id: t.id, name: t.name} END)[0..10] AS teams

            RETURN assets, services, businessFunctions, applications, userJourneys, teams
            """;

    private final Neo4jClient neo4jClient;

    public SearchAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchResult search(String query) {
        return neo4jClient.query(SEARCH_CYPHER)
                .bind(query).to("query")
                .fetch()
                .one()
                .map(row -> new SearchResult(
                        toNodeRefs((List<Map<String, Object>>) row.get("assets")),
                        toNodeRefs((List<Map<String, Object>>) row.get("services")),
                        toNodeRefs((List<Map<String, Object>>) row.get("businessFunctions")),
                        toNodeRefs((List<Map<String, Object>>) row.get("applications")),
                        toNodeRefs((List<Map<String, Object>>) row.get("userJourneys")),
                        toNodeRefs((List<Map<String, Object>>) row.get("teams"))))
                .orElse(new SearchResult(
                        List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));
    }

    private List<NodeRef> toNodeRefs(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(m -> m != null && m.get("id") != null)
                .map(m -> new NodeRef((String) m.get("id"), (String) m.get("name")))
                .toList();
    }
}

