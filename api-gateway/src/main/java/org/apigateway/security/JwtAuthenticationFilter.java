package org.apigateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global WebFlux filter that runs before route predicates.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Skip public routes (login, register, onboarding submission, actuator).</li>
 *   <li>Reject requests without a valid Bearer token with 401.</li>
 *   <li>Enrich downstream requests with identity headers extracted from the JWT
 *       ({@code X-User-Id}, {@code X-Username}, {@code X-Tenant-Id},
 *       {@code X-Tenant-Schema}, {@code X-User-Roles}).</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /**
     * Routes that do NOT require a JWT. Patterns follow Ant-style matching.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/onboarding",          // POST – public request submission
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    @Override
    public int getOrder() {
        // Run before the default NettyRoutingFilter (order = Integer.MAX_VALUE)
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or malformed Authorization header for path: {}", path);
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            log.debug("Invalid or expired JWT for path: {}", path);
            return unauthorized(exchange, "Token is invalid or expired");
        }

        // Extract claims and forward as downstream headers
        String userId       = jwtService.extractUserId(token);
        String username     = jwtService.extractUsername(token);
        String tenantId     = jwtService.extractTenantId(token);
        String tenantSchema = jwtService.extractTenantSchema(token);
        String roles        = String.join(",", jwtService.extractRoles(token));

        ServerHttpRequest enriched = exchange.getRequest().mutate()
                .header("X-User-Id",      nullSafe(userId))
                .header("X-Username",     nullSafe(username))
                .header("X-Tenant-Id",    nullSafe(tenantId))
                .header("X-Tenant-Schema", nullSafe(tenantSchema))
                .header("X-User-Roles",   roles)
                .build();

        log.debug("JWT validated for user={}, tenant={}, path={}", username, tenantSchema, path);

        return chain.filter(exchange.mutate().request(enriched).build());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"success":false,"error":"%s","timestamp":"%s"}
                """.formatted(message, java.time.Instant.now());
        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}