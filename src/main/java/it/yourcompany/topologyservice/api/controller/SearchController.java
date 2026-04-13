package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.SearchResponse;
import it.yourcompany.topologyservice.api.mapper.SearchMapper;
import it.yourcompany.topologyservice.application.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for topology entity search.
 * Always returns a result; empty lists signal no matches — never a 404.
 */
@Validated
@RestController
@RequestMapping(path = "/api/v1/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {

    private final SearchService searchService;
    private final SearchMapper searchMapper;

    public SearchController(SearchService searchService, SearchMapper searchMapper) {
        this.searchService = searchService;
        this.searchMapper = searchMapper;
    }

    /**
     * Case-insensitive partial search across assets, services, business functions,
     * applications, user journeys, and teams.
     *
     * @param query minimum 2 characters to avoid full-graph scans
     */
    @GetMapping
    public SearchResponse search(
            @RequestParam @NotBlank @Size(min = 2, message = "query must be at least 2 characters") String query) {
        return searchMapper.toResponse(query, searchService.search(query));
    }
}

