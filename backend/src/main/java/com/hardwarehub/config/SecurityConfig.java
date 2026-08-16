package com.hardwarehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * IN PROGRESS (Block G). authorizeHttpRequests still says permitAll() —
 * that's rewritten to real rules in G4/G5, once the token filter exists to
 * enforce them against. CSRF stays disabled: this is a stateless REST API
 * using bearer tokens, not a browser session/cookie flow, so CSRF isn't the
 * right protection model here regardless of how auth ends up wired.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
