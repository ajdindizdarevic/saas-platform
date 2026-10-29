package org.onboardingservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "onboarding_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingRequest extends BaseEntity {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_email", nullable = false, unique = true)
    private String contactEmail;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "expected_users")
    private Integer expectedUsers;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "tenant_id")
    private String tenantId;
}