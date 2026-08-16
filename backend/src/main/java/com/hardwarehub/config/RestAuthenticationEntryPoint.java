package com.hardwarehub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardwarehub.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Without this, Spring Security's default behavior returns a bare 403 for
 * missing/invalid tokens — the same code it uses for "authenticated but
 * wrong role" (Block G5). Those are different situations: 401 means "you
 * are not authenticated," 403 means "you are, but you're not allowed."
 * This makes the 401 case explicit and returns it in the same ErrorResponse
 * shape every other error on this API uses.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(401, "Unauthorized", "authentication required")));
    }
}
