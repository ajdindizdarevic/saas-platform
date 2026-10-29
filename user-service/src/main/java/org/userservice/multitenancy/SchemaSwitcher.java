package org.userservice.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        setSearchPath(connection, DEFAULT_SCHEMA);
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
        if (!schema.matches("[a-z0-9_]+")) {
            log.warn("Blocked unsafe schema identifier: '{}'", schema);
            schema = DEFAULT_SCHEMA;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO " + schema + ", public");
        }
    }
}