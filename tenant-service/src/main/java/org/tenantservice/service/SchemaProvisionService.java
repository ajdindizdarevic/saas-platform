package org.tenantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Low-level service responsible for creating and managing PostgreSQL schemas
 * for individual tenants.
 *
 * <p>All DDL is executed directly via {@link JdbcTemplate} (bypassing JPA/Hibernate)
 * so it runs against the {@code public} schema and is not subject to the
 * current tenant's {@code search_path}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaProvisionService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new PostgreSQL schema for the given tenant and runs tenant-level
     * Flyway migrations inside it (delegated to {@link FlywayMigrationService}).
     *
     * @param schemaName validated, slug-form schema identifier
     */
    public void createSchema(String schemaName) {
        validateSchemaName(schemaName);
        log.info("Creating schema '{}'", schemaName);
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        log.info("Schema '{}' created successfully", schemaName);
    }

    /**
     * Drops the schema and all objects within it.
     * Use with extreme care – data loss is irreversible.
     */
    public void dropSchema(String schemaName) {
        validateSchemaName(schemaName);
        if ("public".equals(schemaName)) {
            throw new IllegalArgumentException("Cannot drop the public schema");
        }
        log.warn("Dropping schema '{}' CASCADE", schemaName);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
        log.info("Schema '{}' dropped", schemaName);
    }

    public boolean schemaExists(String schemaName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
                Integer.class, schemaName);
        return count != null && count > 0;
    }

    /**
     * Converts a company name to a safe, lowercase PostgreSQL schema identifier.
     * e.g. "Acme Corp!" → "acme_corp"
     */
    public static String toSchemaName(String companyName) {
        return companyName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .substring(0, Math.min(companyName.length(), 63));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void validateSchemaName(String schemaName) {
        if (schemaName == null || !schemaName.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
    }
}