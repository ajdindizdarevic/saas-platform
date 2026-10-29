package org.tenantservice.model;

public enum TenantStatus {
    /** Schema provisioning in progress */
    PROVISIONING,
    /** Fully active and accessible */
    ACTIVE,
    /** Temporarily suspended (billing, abuse, etc.) */
    SUSPENDED,
    /** Soft-deleted, data retained for grace period */
    DEACTIVATED
}