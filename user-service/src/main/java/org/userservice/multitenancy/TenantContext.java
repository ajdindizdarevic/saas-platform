package org.userservice.multitenancy;


/**
 * Thread-local holder for the current tenant's PostgreSQL schema name.
 * Must be cleared after each request/task to avoid schema leakage.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_SCHEMA = new InheritableThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentSchema(String schema) { CURRENT_SCHEMA.set(schema); }
    public static String getCurrentSchema()            { return CURRENT_SCHEMA.get(); }
    public static void clear()                         { CURRENT_SCHEMA.remove(); }

    public static boolean hasSchema() {
        String s = CURRENT_SCHEMA.get();
        return s != null && !s.isBlank();
    }
}