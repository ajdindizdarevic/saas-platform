package org.tenantservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "tenants",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tenants_schema_name",  columnNames = "schema_name"),
                @UniqueConstraint(name = "uq_tenants_contact_email", columnNames = "contact_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    /** Human-readable company / organisation name */
    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    /**
     * PostgreSQL schema that isolates this tenant's data.
     * Derived from companyName at creation time (lowercase, slug-form).
     */
    @Column(name = "schema_name", nullable = false, length = 63, updatable = false)
    private String schemaName;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "expected_users")
    private Integer expectedUsers;

    /** UUID of the originating onboarding-service request */
    @Column(name = "onboarding_request_id", updatable = false)
    private UUID onboardingRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.PROVISIONING;

    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 50;

    @Column(name = "max_storage_gb")
    @Builder.Default
    private Integer maxStorageGb = 10;

    @Column(name = "plan", length = 50)
    @Builder.Default
    private String plan = "STARTER";
}
