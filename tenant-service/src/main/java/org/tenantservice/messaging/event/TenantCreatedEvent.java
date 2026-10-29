package org.tenantservice.messaging.event;

import lombok.*;

import java.util.UUID;

/**
 * Outbound event published by tenant-service once the tenant schema is fully provisioned.
 * Consumed by user-service (to seed the admin user) and api-gateway (to register the route).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCreatedEvent {

    private String eventId;

    @Builder.Default
    private String eventType = "TENANT_CREATED";

    private UUID tenantId;
    private String schemaName;
    private String companyName;
    private String contactEmail;
    private String contactName;
    private String plan;
    private Integer maxUsers;
    private UUID onboardingRequestId;
}
