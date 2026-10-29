-- V1__create_onboarding_tables.sql

CREATE TABLE IF NOT EXISTS onboarding_requests (
                                                   id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name    VARCHAR(150)    NOT NULL,
    contact_email   VARCHAR(255)    NOT NULL UNIQUE,
    contact_name    VARCHAR(100)    NOT NULL,
    phone_number    VARCHAR(20),
    business_type   VARCHAR(100),
    expected_users  INTEGER,
    notes           TEXT,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    reviewed_by     VARCHAR(100),
    tenant_id       VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_status CHECK (
                                    status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED')
    )
    );

CREATE INDEX idx_onboarding_requests_status      ON onboarding_requests(status);
CREATE INDEX idx_onboarding_requests_email       ON onboarding_requests(contact_email);
CREATE INDEX idx_onboarding_requests_company     ON onboarding_requests(company_name);
CREATE INDEX idx_onboarding_requests_created_at  ON onboarding_requests(created_at DESC);

COMMENT ON TABLE  onboarding_requests                IS 'Stores tenant onboarding requests from prospective customers';
COMMENT ON COLUMN onboarding_requests.status         IS 'Lifecycle state: PENDING → UNDER_REVIEW → APPROVED | REJECTED';
COMMENT ON COLUMN onboarding_requests.tenant_id      IS 'Populated after approval; references the created tenant in tenant-service';