CREATE TABLE t_merchant (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    logo            VARCHAR(500),
    license_no      VARCHAR(50),
    license_images  JSONB,
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(20),
    status          SMALLINT NOT NULL DEFAULT 0,
    commission_rate DECIMAL(5,2) DEFAULT 0,
    settled_at      BIGINT,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
