package org.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Centralised CORS configuration for the API Gateway.
 *
 * <p>All downstream services are reached through this gateway, so CORS only
 * needs to be configured once here. Individual microservices should NOT
 * configure CORS (or should allow {@code *}) since they only receive
 * traffic that has already passed through the gateway.
 */
@Configuration
public class CorsConfig {

    // YAML lists don't bind with @Value; accept comma-separated string instead.
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsRaw;

    @Bean
    public CorsWebFilter corsWebFilter() {
        List<String> allowedOrigins = Arrays.asList(allowedOriginsRaw.split(","));
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-Tenant-Schema",
                "X-Refresh-Token",
                "Accept"
        ));
        config.setExposedHeaders(List.of(
                "Authorization",
                "X-Gateway-Request-Id"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsWebFilter(source);
    }
}