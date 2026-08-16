package com.hardwarehub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardwarehub.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Handles "authenticated, but wrong role" (403) — the counterpart to
 * RestAuthenticationEntryPoint's "not authenticated at all" (401).
 *
 * Writes the response directly instead of calling response.sendError(403).
 * sendError() doesn't write a body itself — it flags the response for the
 * servlet container to perform an internal forward to /error, and that
 * forward re-enters the whole dispatch cycle. By then the security context
 * (a per-request ThreadLocal, cleared once the original filter chain
 * unwinds) is gone, so the forwarded request has no authentication and gets
 * caught by the entry point instead — meaning every 403 would silently come
 * back as a 401 with the wrong message. Writing directly here sidesteps
 * that reprocessing entirely.
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(403, "Forbidden", "you do not have permission to do this")));
    }
}
