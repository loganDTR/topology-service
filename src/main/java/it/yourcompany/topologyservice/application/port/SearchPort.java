package it.yourcompany.topologyservice.application.port;

import it.yourcompany.topologyservice.domain.result.SearchResult;

/**
 * Output port for topology entity search.
 * Always returns a result (possibly with empty lists); never throws for "no results".
 */
public interface SearchPort {

    /**
     * Searches all main entity types for partial id or name matches
     * against the given query string (case-insensitive).
     */
    SearchResult search(String query);
}

