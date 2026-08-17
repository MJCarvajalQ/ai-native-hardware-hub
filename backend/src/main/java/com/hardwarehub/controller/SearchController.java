package com.hardwarehub.controller;

import com.hardwarehub.dto.SearchRequest;
import com.hardwarehub.dto.SearchResultDTO;
import com.hardwarehub.service.SemanticSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SemanticSearchService semanticSearchService;

    public SearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @PostMapping
    public List<SearchResultDTO> search(@Valid @RequestBody SearchRequest request) {
        return semanticSearchService.search(request.query());
    }
}
