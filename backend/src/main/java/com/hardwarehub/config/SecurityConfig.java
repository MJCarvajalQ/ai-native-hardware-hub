package com.hardwarehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORARY — Block F only. CSRF is disabled because this is a stateless
 * REST API, not a browser session flow, so it isn't the right protection
 * model here anyway. permitAll() exists purely so the endpoints built in
 * this block can be curl-tested; it is NOT the real security posture.
 *
 * Block G replaces the permitAll() with real rules: authenticated-only by
 * default, admin-only on the admin endpoints. This class must not still
 * look like this after Block G is done.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
