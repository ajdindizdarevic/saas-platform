-- V1__create_user_tables.sql
-- Runs in the PUBLIC schema; manages platform-level user/role tables.
-- Tenant-specific user data lives in per-tenant schemas (provisioned by tenant-service).

-- ── roles ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS roles (
                                     id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(30)  NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_role_name CHECK (
                                       name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')
    )
    );

-- ── users ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
                                     id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    email                VARCHAR(255) NOT NULL,
    username             VARCHAR(100) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    phone_number         VARCHAR(20),
    tenant_id            VARCHAR(100),
    tenant_schema        VARCHAR(63),
    onboarding_request_id UUID,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at        TIMESTAMP,
    profile_picture_url  TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
    );

-- ── user_roles (join table) ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
    );

-- ── indexes ───────────────────────────────────────────────────────────────────

CREATE INDEX idx_users_email        ON users(email);
CREATE INDEX idx_users_username     ON users(username);
CREATE INDEX idx_users_tenant_id    ON users(tenant_id);
CREATE INDEX idx_users_tenant_schema ON users(tenant_schema);
CREATE INDEX idx_users_active       ON users(is_active);
CREATE INDEX idx_user_roles_user    ON user_roles(user_id);
CREATE INDEX idx_user_roles_role    ON user_roles(role_id);

-- ── seed default roles ────────────────────────────────────────────────────────

INSERT INTO roles (name, description) VALUES
                                          ('SUPER_ADMIN', 'Platform owner; full access across all tenants'),
                                          ('ADMIN',       'Tenant administrator; full access within a single tenant'),
                                          ('MANAGER',     'Team/department lead; elevated permissions within a tenant'),
                                          ('USER',        'Standard tenant user')
    ON CONFLICT (name) DO NOTHING;

-- ── comments ──────────────────────────────────────────────────────────────────

COMMENT ON TABLE  users               IS 'Platform users; each row belongs to one tenant';
COMMENT ON COLUMN users.tenant_id     IS 'Logical tenant identifier (UUID as text)';
COMMENT ON COLUMN users.tenant_schema IS 'PostgreSQL schema name for the user''s tenant';
COMMENT ON TABLE  roles               IS 'Application-level roles seeded at startup';