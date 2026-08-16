package com.hardwarehub.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * What the admin's "Add New Device" form sends. New hardware always starts
 * as AVAILABLE — the admin can toggle it to REPAIR afterward if needed, so
 * status isn't part of this request.
 */
public record CreateHardwareRequest(
        @NotBlank String name,
        @NotBlank String brand,
        LocalDate purchaseDate,
        String notes
) {
}
