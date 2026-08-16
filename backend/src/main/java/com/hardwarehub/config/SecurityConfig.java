package com.hardwarehub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardwarehub.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * IN PROGRESS (Block G). CSRF stays disabled throughout: this is a
 * stateless REST API using bearer tokens, not a browser session/cookie
 * flow, so CSRF isn't the right protection model here regardless of how
 * auth ends up wired.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, TokenStore tokenStore, UserRepository userRepository, ObjectMapper objectMapper
    ) throws Exception {
        var tokenFilter = new TokenAuthenticationFilter(tokenStore, userRepository);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        // hardware: any authenticated user can list/rent/return;
                        // only an admin can add, delete, or toggle repair status
                        .requestMatchers(HttpMethod.POST, "/api/hardware").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/hardware/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/hardware/*/repair").hasRole("ADMIN")
                        // account creation is admin-only, and the only way in —
                        // there is no self-registration endpoint anywhere
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
