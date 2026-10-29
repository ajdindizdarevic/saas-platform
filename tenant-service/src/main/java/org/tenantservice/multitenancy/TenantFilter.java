package org.tenantservice.multitenancy;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Servlet filter that resolves the current tenant schema and stores it in
 * {@link TenantContext} for the duration of the request.
 *
 * <p>The filter runs early (Order = 1) so that all downstream components
 * (Security, Spring MVC, Hibernate) see the correct schema.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private final TenantResolver tenantResolver;

    /** Paths that don't require a tenant context */
    private static final String[] PUBLIC_PATHS = {
            "/actuator", "/v3/api-docs", "/swagger-ui"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;

        if (isPublicPath(httpReq.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String schema = tenantResolver.resolve(httpReq);
            if (StringUtils.hasText(schema)) {
                TenantContext.setCurrentSchema(schema);
                log.debug("TenantFilter: set schema='{}' for {}", schema, httpReq.getRequestURI());
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isPublicPath(String uri) {
        if (uri == null) return false;
        for (String prefix : PUBLIC_PATHS) {
            if (uri.startsWith(prefix)) return true;
        }
        return false;
    }
}
