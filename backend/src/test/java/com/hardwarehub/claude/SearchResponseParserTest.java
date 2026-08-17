package com.hardwarehub.claude;

import com.hardwarehub.exception.SearchUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchResponseParserTest {

    private final SearchResponseParser parser = new SearchResponseParser();
    private final Set<Long> validIds = Set.of(1L, 2L, 3L);

    @Test
    void parsesPlainJsonArray() {
        String raw = "[{\"id\": 1, \"reason\": \"matches\"}, {\"id\": 2, \"reason\": \"also matches\"}]";
        List<SearchResponseParser.ParsedResult> results = parser.parse(raw, validIds);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).id());
    }

    @Test
    void stripsMarkdownCodeFences() {
        String raw = "```json\n[{\"id\": 1, \"reason\": \"matches\"}]\n```";
        List<SearchResponseParser.ParsedResult> results = parser.parse(raw, validIds);
        assertEquals(1, results.size());
    }

    @Test
    void dropsUnknownIds() {
        String raw = "[{\"id\": 1, \"reason\": \"ok\"}, {\"id\": 999, \"reason\": \"unknown item\"}]";
        List<SearchResponseParser.ParsedResult> results = parser.parse(raw, validIds);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).id());
    }

    @Test
    void throwsOnMalformedJsonInsteadOfCrashing() {
        assertThrows(SearchUnavailableException.class,
                () -> parser.parse("this is not json at all", validIds));
    }

    @Test
    void throwsWhenResponseIsProseNotAnArray() {
        assertThrows(SearchUnavailableException.class,
                () -> parser.parse("Sure! Here are the matching items: item 1 and item 2.", validIds));
    }

    @Test
    void returnsEmptyListForEmptyArray() {
        List<SearchResponseParser.ParsedResult> results = parser.parse("[]", validIds);
        assertTrue(results.isEmpty());
    }
}
