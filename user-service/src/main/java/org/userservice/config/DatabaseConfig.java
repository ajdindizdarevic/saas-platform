package org.userservice.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import org.userservice.multitenancy.SchemaSwitcher;

import java.util.Map;

/**
 * Registers the Spring-managed {@link SchemaSwitcher} bean directly with
 * Hibernate as both the {@link org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider}
 * and the {@link org.hibernate.context.spi.CurrentTenantIdentifierResolver}.
 *
 * <p>Hibernate 7 (Spring Boot 4) requires these to be provided as instances, not
 * class-name strings, so they must be injected from the Spring context.
 * Spring Boot's {@link HibernatePropertiesCustomizer} is the recommended hook.
 */
@Configuration
public class DatabaseConfig implements HibernatePropertiesCustomizer {

    private final SchemaSwitcher schemaSwitcher;

    public DatabaseConfig(SchemaSwitcher schemaSwitcher) {
        this.schemaSwitcher = schemaSwitcher;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(
                AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, schemaSwitcher);
        hibernateProperties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, schemaSwitcher);
    }
}