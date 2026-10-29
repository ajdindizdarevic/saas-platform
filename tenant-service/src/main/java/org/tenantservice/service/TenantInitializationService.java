package org.tenantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenantservice.messaging.event.TenantApprovedEvent;
import org.tenantservice.messaging.event.TenantCreatedEvent;
import org.tenantservice.messaging.producer.TenantCreatedProducer;
import org.tenantservice.model.Tenant;
import org.tenantservice.model.TenantStatus;
import org.tenantservice.repository.TenantRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitializationService {

    private final DataSource          dataSource;
    private final TenantCreatedProducer tenantCreatedProducer;
    private final TenantRepository    tenantRepository;

    /**
     * Main tenant provisioning pipeline:
     * 1. Create tenant schema
     * 2. Initialize schema resources/tables
     * 3. Publish TenantCreatedEvent
     */
    @Transactional
    public void initialize(TenantApprovedEvent event) {

        UUID tenantId = event.getOnboardingRequestId();
        String schemaName = buildSchemaName(tenantId.toString());

        log.info("Starting tenant initialization for tenantId={}, schema={}",
                tenantId, schemaName);

        // 1. Create schema
        createSchema(schemaName);

        // 2. Initialize tables/resources inside the schema
        initializeSchemaResources(schemaName);

        // 3. Persist the Tenant record
        Tenant tenant = Tenant.builder()
                .companyName(event.getCompanyName())
                .schemaName(schemaName)
                .contactEmail(event.getContactEmail())
                .contactName(event.getContactName())
                .phoneNumber(event.getPhoneNumber())
                .businessType(event.getBusinessType())
                .expectedUsers(event.getExpectedUsers())
                .onboardingRequestId(event.getOnboardingRequestId())
                .status(TenantStatus.ACTIVE)
                .build();
        // Use the UUID from the onboarding request as the tenant ID
        // (avoids a separate ID generation and keeps traceability)
        tenantRepository.save(tenant);
        log.info("Tenant record saved: id={}, schema={}", tenant.getId(), schemaName);

        // 4. Publish TenantCreatedEvent for downstream services (user-service creates admin)
        TenantCreatedEvent createdEvent = TenantCreatedEvent.builder()
                .tenantId(tenant.getId())
                .companyName(event.getCompanyName())
                .contactEmail(event.getContactEmail())
                .contactName(event.getContactName())
                .schemaName(schemaName)
                .onboardingRequestId(event.getOnboardingRequestId())
                .build();

        tenantCreatedProducer.publish(createdEvent);

        log.info("Tenant provisioning completed successfully for tenantId={}",
                tenant.getId());
    }

    /**
     * Creates PostgreSQL schema for tenant.
     */
    private void createSchema(String schemaName) {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;

        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {

            statement.execute(sql);

            log.info("Schema created successfully: {}", schemaName);

        } catch (Exception ex) {
            log.error("Failed to create schema {}: {}", schemaName, ex.getMessage(), ex);
            throw new RuntimeException("Failed to create tenant schema", ex);
        }
    }

    /**
     * Initializes schema resources.
     * You can later add Flyway/Liquibase migrations here.
     */
    private void initializeSchemaResources(String schemaName) {

        log.info("Initializing resources for schema={}", schemaName);

        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {

            // Example table creation
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS %s.users (
                        id UUID PRIMARY KEY,
                        username VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """.formatted(schemaName));

            log.info("Default tables created for schema={}", schemaName);

        } catch (Exception ex) {
            log.error("Failed initializing schema resources for {}: {}",
                    schemaName, ex.getMessage(), ex);

            throw new RuntimeException("Failed to initialize schema resources", ex);
        }
    }

    /**
     * Generates schema name from tenant ID.
     */
    private String buildSchemaName(String tenantId) {
        return "tenant_" + tenantId.replace("-", "_");
    }
}