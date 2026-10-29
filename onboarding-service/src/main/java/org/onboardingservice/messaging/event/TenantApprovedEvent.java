package org.onboardingservice.messaging.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantApprovedEvent {

    private UUID   onboardingRequestId;
    private String companyName;
    private String contactEmail;
    private String contactName;
    private String phoneNumber;
    private String businessType;
    private Integer expectedUsers;
    private String reviewedBy;
    private String eventId;

    @Builder.Default
    private String eventType = "TENANT_APPROVED";
}