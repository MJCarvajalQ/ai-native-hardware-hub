package com.hardwarehub.dto;

import com.hardwarehub.model.Role;
import com.hardwarehub.model.User;

/**
 * Never includes passwordHash — this is the only user shape that ever
 * leaves the server.
 */
public record UserDTO(Long id, String email, Role role) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getRole());
    }
}
