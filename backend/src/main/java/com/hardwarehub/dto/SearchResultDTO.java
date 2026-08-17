package com.hardwarehub.dto;

/**
 * One entry Claude's ranked-search response is parsed into: which hardware
 * id it means, and why it judged the item relevant.
 */
public record SearchResultDTO(HardwareDTO hardware, String reason) {
}
