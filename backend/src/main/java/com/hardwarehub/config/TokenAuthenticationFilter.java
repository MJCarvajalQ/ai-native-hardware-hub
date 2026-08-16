package com.hardwarehub.config;

import com.hardwarehub.model.User;
import com.hardwarehub.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Resolves "Authorization: Bearer <token>" into an authenticated principal.
 * Always re-reads the User from the database rather than trusting anything
 * cached in the token — a role change or account deletion takes effect on
 * the very next request. Missing/invalid tokens are left unauthenticated
 * here (not rejected outright); authorizeHttpRequests (SecurityConfig)
 * decides what to do about that per endpoint.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenStore tokenStore;
    private final UserRepository userRepository;

    public TokenAuthenticationFilter(TokenStore tokenStore, UserRepository userRepository) {
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            Optional<User> user = tokenStore.resolve(token).flatMap(userRepository::findById);

            user.ifPresent(u -> {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
                var authentication = new UsernamePasswordAuthenticationToken(u, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
