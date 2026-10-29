-- V1: Create tenants table in public schema

CREATE TABLE IF NOT EXISTS public.tenants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name        VARCHAR(150) NOT NULL,
    schema_name         VARCHAR(63)  NOT NULL,
    contact_email       VARCHAR(255) NOT NULL,
    contact_name        VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20),
    business_type       VARCHAR(100),
    expected_users      INTEGER,
    onboarding_request_id UUID,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PROVISIONING',
    max_users           INTEGER      NOT NULL DEFAULT 50,
    max_storage_gb      INTEGER      NOT NULL DEFAULT 10,
    plan                VARCHAR(50)  NOT NULL DEFAULT 'STARTER',
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenants_schema_name   UNIQUE (schema_name),
    CONSTRAINT uq_tenants_contact_email UNIQUE (contact_email)
);

CREATE INDEX IF NOT EXISTS idx_tenants_status ON public.tenants (status);
CREATE INDEX IF NOT EXISTS idx_tenants_onboarding_request_id ON public.tenants (onboarding_request_id);