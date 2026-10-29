package org.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route definitions.
 *
 * <p>Routes defined here complement (and take precedence over) those in
 * {@code application.yml}. For simpler maintenance all routes are defined
 * in {@code application.yml}; this class exists for routes that need
 * conditional logic or cannot be expressed declaratively.
 *
 * <p>The three microservices:
 * <ul>
 *   <li><b>user-service</b>      — port 8082 — handles auth and user CRUD</li>
 *   <li><b>onboarding-service</b> — port 8081 — handles tenant onboarding requests</li>
 *   <li><b>tenant-service</b>    — port 8083 — manages provisioned tenants</li>
 * </ul>
 */
@Configuration
public class GatewayConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.onboarding-service.url}")
    private String onboardingServiceUrl;

    @Value("${services.tenant-service.url}")
    private String tenantServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── user-service ───────────────────────────────────────────────
                .route("user-service-auth", r -> r
                        .path("/api/v1/auth/**")
                        .uri(userServiceUrl))

                .route("user-service-users", r -> r
                        .path("/api/v1/users/**")
                        .uri(userServiceUrl))

                // ── onboarding-service ─────────────────────────────────────────
                .route("onboarding-public", r -> r
                        .path("/api/v1/onboarding/**")
                        .uri(onboardingServiceUrl))

                .route("onboarding-admin", r -> r
                        .path("/api/v1/admin/onboarding/**")
                        .uri(onboardingServiceUrl))

                // ── tenant-service ─────────────────────────────────────────────
                .route("tenant-service", r -> r
                        .path("/api/v1/tenants/**")
                        .uri(tenantServiceUrl))

                .build();
    }
}