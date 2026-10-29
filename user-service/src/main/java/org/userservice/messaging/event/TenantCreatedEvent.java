package org.userservice.messaging.event;

import lombok.*;

import java.util.UUID;

/**
 * Inbound event published by tenant-service once a tenant's schema is provisioned.
 * The user-service consumes this to seed the initial tenant admin user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCreatedEvent {

    private String        eventId;
    private String        eventType;
    private UUID          tenantId;
    private String        schemaName;
    private String        companyName;
    private String        contactEmail;
    private String        contactName;
    private String        plan;
    private Integer       maxUsers;
    private UUID          onboardingRequestId;
}