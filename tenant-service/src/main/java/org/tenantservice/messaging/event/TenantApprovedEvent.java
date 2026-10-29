package org.tenantservice.messaging.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound event published by onboarding-service when an onboarding request is approved.
 * The tenant-service consumes this to trigger tenant creation and schema provisioning.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantApprovedEvent {

    private String eventId;
    private String eventType;
    private UUID onboardingRequestId;
    private String companyName;
    private String contactEmail;
    private String contactName;
    private String phoneNumber;
    private String businessType;
    private Integer expectedUsers;
    private String reviewedBy;
    private LocalDateTime approvedAt;
}