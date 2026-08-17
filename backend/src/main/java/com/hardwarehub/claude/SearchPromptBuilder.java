package com.hardwarehub.claude;

import com.hardwarehub.model.Hardware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the prompt sent to Claude for semantic search. Includes notes and
 * history for every item, not just name/brand/status — the whole point of
 * this feature is judging condition from free-text notes (e.g. catching a
 * swollen-battery device marked Available), which a keyword match can't do.
 * Dropping notes to save tokens would remove the feature's entire value.
 */
@Component
public class SearchPromptBuilder {

    public String build(String query, List<Hardware> inventory) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a search assistant for an internal hardware rental system. ");
        sb.append("Given a user query and a JSON inventory list, return the items relevant to the query, ");
        sb.append("ranked most relevant first. Use the notes and history fields to judge condition and ");
        sb.append("suitability, not just name/brand keyword matches — for example, a device with damage ");
        sb.append("noted should be down-ranked or excluded even if its status is Available.\n\n");

        sb.append("Respond with ONLY a JSON array, no other text, no markdown code fences. ");
        sb.append("Each element must be: {\"id\": <number>, \"reason\": \"<short explanation>\"}. ");
        sb.append("Only include items actually relevant to the query. If nothing is relevant, return [].\n\n");

        sb.append("Query: ").append(query).append("\n\n");

        sb.append("Inventory:\n");
        for (Hardware item : inventory) {
            sb.append("- id=").append(item.getId())
                    .append(", name=").append(item.getName())
                    .append(", brand=").append(item.getBrand())
                    .append(", status=").append(item.getStatus())
                    .append(", notes=").append(item.getNotes() == null ? "" : item.getNotes())
                    .append(", history=").append(item.getHistory() == null ? "" : item.getHistory())
                    .append("\n");
        }

        return sb.toString();
    }
}
