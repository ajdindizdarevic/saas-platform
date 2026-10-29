package org.tenantservice.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Dual-purpose Hibernate multi-tenancy component:
 * <ul>
 *   <li>Implements {@link MultiTenantConnectionProvider} – acquires a pooled
 *       connection and switches the PostgreSQL {@code search_path} to the
 *       tenant schema before handing it to Hibernate.</li>
 *   <li>Implements {@link CurrentTenantIdentifierResolver} – reads the current
 *       schema name from {@link TenantContext}.</li>
 * </ul>
 *
 * Registered in {@link org.saas.tenant.config.DatabaseConfig}.
 */
@Slf4j
@Component
public class SchemaSwitcher
        implements MultiTenantConnectionProvider<String>, CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA  = "public";
    private static final long   serialVersionUID = 1L;

    private final DataSource dataSource;

    public SchemaSwitcher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ── CurrentTenantIdentifierResolver ──────────────────────────────────────

    @Override
    public String resolveCurrentTenantIdentifier() {
        String schema = TenantContext.getCurrentSchema();
        return (schema != null && !schema.isBlank()) ? schema : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    // ── MultiTenantConnectionProvider ────────────────────────────────────────

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        Connection conn = dataSource.getConnection();
        setSearchPath(conn, schema);
        return conn;
    }

    @Override
    public void releaseConnection(String schema, Connection connection) throws SQLException {
        setSearchPath(connection, DEFAULT_SCHEMA); // restore before returning to pool
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap SchemaSwitcher as " + unwrapType);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void setSearchPath(Connection conn, String schema) throws SQLException {
        // Sanitise: allow only safe identifier characters to prevent SQL injection
        if (!schema.matches("[a-z0-9_]+")) {
            log.warn("Blocked unsafe schema name: '{}'", schema);
            schema = DEFAULT_SCHEMA;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO " + schema + ", public");
            log.trace("search_path set to '{}'", schema);
        }
    }
}