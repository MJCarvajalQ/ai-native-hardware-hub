package com.hardwarehub.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(@NotBlank String query) {
}
