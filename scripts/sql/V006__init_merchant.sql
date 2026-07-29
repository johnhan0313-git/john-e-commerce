-- V006: merchant domain

CREATE TABLE t_merchant (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    logo            VARCHAR(500),
    license_no      VARCHAR(50),
    license_images  JSONB,
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(20),
    status          SMALLINT NOT NULL DEFAULT 0,
    commission_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    settled_at      BIGINT,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_merchant_tenant ON t_merchant (tenant_id);
CREATE INDEX idx_t_merchant_user ON t_merchant (tenant_id, user_id);
CREATE INDEX idx_t_merchant_status ON t_merchant (tenant_id, status);

COMMENT ON TABLE t_merchant IS 'Merchant onboarding profile';
