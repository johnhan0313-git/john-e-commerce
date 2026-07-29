-- V002: user domain

CREATE TABLE t_user (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    nickname        VARCHAR(50),
    avatar          VARCHAR(500),
    user_type       SMALLINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    password_hash   VARCHAR(200),
    wx_openid       VARCHAR(100),
    wx_unionid      VARCHAR(100),
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_user_phone ON t_user (tenant_id, phone) WHERE delete_flag = 0 AND phone IS NOT NULL;
CREATE INDEX idx_t_user_tenant ON t_user (tenant_id);
CREATE INDEX idx_t_user_wx_openid ON t_user (tenant_id, wx_openid) WHERE wx_openid IS NOT NULL;

COMMENT ON TABLE t_user IS 'Tenant end-user / admin account';

CREATE TABLE t_user_address (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(50),
    phone           VARCHAR(20),
    province        VARCHAR(50),
    city            VARCHAR(50),
    district        VARCHAR(50),
    detail          VARCHAR(200),
    postal_code     VARCHAR(20),
    is_default      BOOLEAN NOT NULL DEFAULT false,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_user_address_tenant ON t_user_address (tenant_id);
CREATE INDEX idx_t_user_address_user ON t_user_address (tenant_id, user_id);

COMMENT ON TABLE t_user_address IS 'User shipping address';
