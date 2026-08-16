package com.hardwarehub.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Carries who is renting/returning. A plain request field rather than a
 * resolved authenticated principal, since login doesn't exist until Block
 * G — the frontend built in Block H will pass the logged-in user's email
 * here, and Block G's auth work replaces this with a resolved principal
 * without HardwareService needing to change at all.
 */
public record RentRequest(@NotBlank String userEmail) {
}
