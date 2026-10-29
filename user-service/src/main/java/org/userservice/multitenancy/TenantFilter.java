package org.userservice.multitenancy;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.userservice.service.JwtService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private final JwtService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";

    private static final String[] SKIP_PATHS = {
            "/api/v1/auth/", "/actuator", "/v3/api-docs", "/swagger-ui"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;

        if (shouldSkip(httpReq.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String schema = resolveSchema(httpReq);
            if (StringUtils.hasText(schema)) {
                TenantContext.setCurrentSchema(schema);
                log.debug("TenantFilter: schema='{}' for {}", schema, httpReq.getRequestURI());
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveSchema(HttpServletRequest request) {
        // 1. JWT claim
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            try {
                return jwtService.extractTenantSchema(header.substring(BEARER_PREFIX.length()));
            } catch (Exception ex) {
                log.trace("Could not extract schema from JWT: {}", ex.getMessage());
            }
        }
        // 2. Fallback header (service-to-service)
        return request.getHeader("X-Tenant-Schema");
    }

    private boolean shouldSkip(String uri) {
        if (uri == null) return false;
        for (String prefix : SKIP_PATHS) {
            if (uri.startsWith(prefix)) return true;
        }
        return false;
    }
}