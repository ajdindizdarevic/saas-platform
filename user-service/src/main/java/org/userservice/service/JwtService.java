package org.userservice.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.userservice.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;   // ms

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;  // ms

    // ── token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles",        principal.getRoleNames());
        claims.put("tenantId",     principal.getTenantId());
        claims.put("tenantSchema", principal.getTenantSchema());
        claims.put("email",        principal.getEmail());
        claims.put("userId",       principal.getId().toString());
        return buildToken(claims, principal.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(new HashMap<>(), principal.getUsername(), refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey())
                .compact();
    }

    // ── token validation ──────────────────────────────────────────────────────

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── claim extraction ──────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTenantSchema(String token) {
        return extractClaim(token, c -> c.get("tenantSchema", String.class));
    }

    public String extractTenantId(String token) {
        return extractClaim(token, c -> c.get("tenantId", String.class));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // return seconds
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}