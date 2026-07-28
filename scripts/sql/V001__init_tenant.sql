CREATE TABLE t_tenant (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(50) UNIQUE NOT NULL,
    business_types  JSONB NOT NULL DEFAULT '[]',
    status          SMALLINT NOT NULL DEFAULT 1,
    config          JSONB DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_tenant_config (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    config_key      VARCHAR(100) NOT NULL,
    config_value    JSONB NOT NULL DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64),
    UNIQUE(tenant_id, config_key)
);
