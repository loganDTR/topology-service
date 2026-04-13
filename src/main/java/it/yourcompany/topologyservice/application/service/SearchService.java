package it.yourcompany.topologyservice.application.service;

import it.yourcompany.topologyservice.application.port.SearchPort;
import it.yourcompany.topologyservice.domain.result.SearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates topology entity search.
 * Always returns a result; empty lists signal no matches.
 */
@Service
public class SearchService {

    private final SearchPort port;

    public SearchService(SearchPort port) {
        this.port = port;
    }

    @Transactional(readOnly = true)
    public SearchResult search(String query) {
        return port.search(query);
    }
}

