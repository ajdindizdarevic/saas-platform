package org.apigateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * WebFlux (reactive) security configuration for the API Gateway.
 *
 * <p>The gateway itself does NOT authenticate users — that is the responsibility
 * of {@link JwtAuthenticationFilter} (a {@link org.springframework.cloud.gateway.filter.GlobalFilter}).
 * Spring Security here provides a safety net: CSRF is disabled (stateless REST),
 * and all paths are permitted at the Spring Security layer so the JWT filter can
 * handle its own 401 responses with consistent JSON bodies.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authEntryPoint;

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint))
                .authorizeExchange(exchanges -> exchanges
                        // Public auth endpoints
                        .pathMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh").permitAll()
                        // Public onboarding submission
                        .pathMatchers(HttpMethod.POST, "/api/v1/onboarding").permitAll()
                        // Observability & docs
                        .pathMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Everything else goes through the JWT GlobalFilter
                        .anyExchange().permitAll()
                )
                .build();
    }
}