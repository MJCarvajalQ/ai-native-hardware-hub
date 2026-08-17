package com.hardwarehub.service;

import com.hardwarehub.claude.ClaudeClient;
import com.hardwarehub.claude.SearchPromptBuilder;
import com.hardwarehub.claude.SearchResponseParser;
import com.hardwarehub.dto.HardwareDTO;
import com.hardwarehub.dto.SearchResultDTO;
import com.hardwarehub.exception.SearchUnavailableException;
import com.hardwarehub.model.Hardware;
import com.hardwarehub.repository.HardwareRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private final HardwareRepository hardwareRepository;
    private final ClaudeClient claudeClient;
    private final SearchPromptBuilder promptBuilder;
    private final SearchResponseParser responseParser;

    public SemanticSearchService(
            HardwareRepository hardwareRepository,
            ClaudeClient claudeClient,
            SearchPromptBuilder promptBuilder,
            SearchResponseParser responseParser
    ) {
        this.hardwareRepository = hardwareRepository;
        this.claudeClient = claudeClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    public List<SearchResultDTO> search(String query) {
        List<Hardware> inventory = hardwareRepository.findAll();
        Map<Long, Hardware> byId = inventory.stream()
                .collect(Collectors.toMap(Hardware::getId, h -> h, (a, b) -> a, LinkedHashMap::new));

        String prompt = promptBuilder.build(query, inventory);

        String rawResponse;
        try {
            rawResponse = claudeClient.sendMessage(prompt);
        } catch (Exception e) {
            throw new SearchUnavailableException("search request failed", e);
        }

        List<SearchResponseParser.ParsedResult> parsed = responseParser.parse(rawResponse, byId.keySet());

        return parsed.stream()
                .map(result -> new SearchResultDTO(HardwareDTO.from(byId.get(result.id())), result.reason()))
                .toList();
    }
}
