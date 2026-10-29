package org.tenantservice.multitenancy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Resolves the current tenant schema name from the incoming HTTP request.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>JWT claim {@code tenantSchema} (preferred – authoritative)
 *   <li>Custom header {@code X-Tenant-Schema} (useful for internal service calls)
 * </ol>
 */
@Slf4j
@Component
public class TenantResolver {

    private static final String BEARER_PREFIX   = "Bearer ";
    private static final String AUTH_HEADER     = "Authorization";
    private static final String TENANT_HEADER   = "X-Tenant-Schema";
    private static final String SCHEMA_CLAIM    = "tenantSchema";

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Returns the schema name, or {@code null} if it cannot be determined.
     */
    public String resolve(HttpServletRequest request) {
        // 1. Try JWT
        String schema = resolveFromJwt(request);
        if (StringUtils.hasText(schema)) {
            return schema;
        }
        // 2. Try header (internal calls / admin)
        schema = request.getHeader(TENANT_HEADER);
        if (StringUtils.hasText(schema)) {
            log.debug("Resolved tenant schema from header: {}", schema);
            return schema;
        }
        return null;
    }

    private String resolveFromJwt(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String schema = claims.get(SCHEMA_CLAIM, String.class);
            if (StringUtils.hasText(schema)) {
                log.debug("Resolved tenant schema from JWT: {}", schema);
            }
            return schema;
        } catch (Exception ex) {
            log.trace("Could not extract tenant schema from JWT: {}", ex.getMessage());
            return null;
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}