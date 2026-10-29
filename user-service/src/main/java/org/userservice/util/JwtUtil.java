package org.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Stateless helper for one-off JWT parsing (e.g. filters, tests).
 * Prefer {@link org.userservice.service.JwtService} for full token lifecycle management.
 */
@Slf4j
@UtilityClass
public class JwtUtil {

    public static Claims parseClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(signingKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String extractUsername(String token, String secret) {
        return parseClaims(token, secret).getSubject();
    }

    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(String token, String secret) {
        Object roles = parseClaims(token, secret).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    public static String extractTenantSchema(String token, String secret) {
        return parseClaims(token, secret).get("tenantSchema", String.class);
    }

    public static boolean isExpired(String token, String secret) {
        try {
            return parseClaims(token, secret).getExpiration()
                    .before(new java.util.Date());
        } catch (Exception ex) {
            log.warn("JWT expiry check failed: {}", ex.getMessage());
            return true;
        }
    }

    private static SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}