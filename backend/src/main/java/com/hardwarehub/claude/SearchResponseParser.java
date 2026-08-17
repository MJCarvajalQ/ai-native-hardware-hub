package com.hardwarehub.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardwarehub.exception.SearchUnavailableException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns Claude's raw text response into a list of (id, reason) pairs.
 * Claude is asked for bare JSON but LLMs are not guaranteed to follow that
 * instruction exactly, so this defensively strips markdown code fences,
 * validates shape, and drops any id that doesn't exist in the inventory —
 * rather than trusting the model's output blindly. Never throws for
 * malformed input; callers get an empty list or a SearchUnavailableException.
 */
@Component
public class SearchResponseParser {

    private static final Pattern CODE_FENCE = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record ParsedResult(Long id, String reason) {
    }

    public List<ParsedResult> parse(String rawText, Set<Long> validIds) {
        String cleaned = stripCodeFences(rawText).trim();

        JsonNode root;
        try {
            root = objectMapper.readTree(cleaned);
        } catch (Exception e) {
            throw new SearchUnavailableException("could not parse search response as JSON", e);
        }

        if (!root.isArray()) {
            throw new SearchUnavailableException("expected a JSON array from search response");
        }

        List<ParsedResult> results = new ArrayList<>();
        for (JsonNode element : root) {
            if (!element.isObject() || !element.has("id") || !element.get("id").isNumber()) {
                continue;
            }
            long id = element.get("id").asLong();
            if (!validIds.contains(id)) {
                continue;
            }
            String reason = element.has("reason") && element.get("reason").isTextual()
                    ? element.get("reason").asText()
                    : "";
            results.add(new ParsedResult(id, reason));
        }
        return results;
    }

    private String stripCodeFences(String text) {
        Matcher matcher = CODE_FENCE.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return text;
    }
}
