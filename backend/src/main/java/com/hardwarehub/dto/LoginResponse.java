package com.hardwarehub.dto;

import com.hardwarehub.model.Role;

/**
 * Includes email and role alongside the token — no extra round-trip needed
 * (a GET /auth/me endpoint) since the login flow already has this data on
 * hand. The frontend needs role to hide admin-only actions from regular
 * users, and email to know whether "IN_USE" means the current user's own
 * rental (show Return) or someone else's (show disabled).
 */
public record LoginResponse(String token, String email, Role role) {
}
