package org.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Runs AFTER {@link org.saas.gateway.security.JwtAuthenticationFilter} (order = -99).
 *
 * <p>Guarantees that every downstream request carries:
 * <ul>
 *   <li>{@code X-Tenant-Schema} — copied from the JWT-enriched header or the
 *       original request header (for service-to-service calls that use the
 *       header directly).</li>
 *   <li>{@code X-Forwarded-For} — preserves the client IP.</li>
 *   <li>{@code X-Gateway-Request-Id} — unique per request for distributed tracing.</li>
 * </ul>
 *
 * <p>If neither the JWT filter nor the client supplied a tenant schema,
 * the header is omitted and downstream services fall back to the {@code public} schema.
 */
@Slf4j
@Component
public class TenantHeaderFilter implements GlobalFilter, Ordered {

    private static final String TENANT_SCHEMA_HEADER = "X-Tenant-Schema";
    private static final String GATEWAY_REQUEST_ID   = "X-Gateway-Request-Id";

    @Override
    public int getOrder() {
        // Run right after JwtAuthenticationFilter (-100)
        return -99;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId   = java.util.UUID.randomUUID().toString();
        String clientIp    = resolveClientIp(exchange);
        String tenantSchema = request.getHeaders().getFirst(TENANT_SCHEMA_HEADER);

        ServerHttpRequest.Builder mutated = request.mutate()
                .header(GATEWAY_REQUEST_ID, requestId);

        // Preserve or forward tenant schema if already present
        if (StringUtils.hasText(tenantSchema)) {
            mutated.header(TENANT_SCHEMA_HEADER, tenantSchema);
        }

        // Always forward client IP
        mutated.header("X-Forwarded-For", clientIp);

        log.debug("TenantHeaderFilter: requestId={}, tenant={}, path={}",
                requestId, tenantSchema, request.getPath().value());

        return chain.filter(exchange.mutate().request(mutated.build()).build());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String resolveClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // take the first (original) address from a chain
            return xForwardedFor.split(",")[0].trim();
        }
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
}