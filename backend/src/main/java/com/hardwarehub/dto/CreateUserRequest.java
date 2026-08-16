package com.hardwarehub.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * No role field on purpose: every account created through this endpoint is
 * a plain USER. The task's Admin Command Center is about managing regular
 * users, not minting more admins, and leaving role out of the request body
 * closes off a privilege-escalation path (a client can't ask to be made
 * ADMIN) rather than needing a runtime check to block it.
 */
public record CreateUserRequest(@NotBlank String email, @NotBlank String password) {
}
