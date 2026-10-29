package org.tenantservice.multitenancy;

/**
 * Holds the current tenant's schema name in a thread-local variable.
 *
 * <p>This is the single source of truth used by {@link SchemaSwitcher}
 * when Hibernate/JDBC needs to target the correct PostgreSQL schema.
 *
 * <p>Always call {@link #clear()} in a {@code finally} block (or via the
 * {@link TenantFilter}) to prevent schema leakage across requests.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_SCHEMA = new InheritableThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentSchema(String schemaName) {
        CURRENT_SCHEMA.set(schemaName);
    }

    public static String getCurrentSchema() {
        return CURRENT_SCHEMA.get();
    }

    public static void clear() {
        CURRENT_SCHEMA.remove();
    }

    public static boolean hasSchema() {
        String schema = CURRENT_SCHEMA.get();
        return schema != null && !schema.isBlank();
    }
}