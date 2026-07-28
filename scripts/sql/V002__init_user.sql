CREATE TABLE t_user (
    id              BIGSERIAL PRIMARY KEY,
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
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64),
    UNIQUE(tenant_id, phone)
);

CREATE TABLE t_user_address (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(50),
    phone           VARCHAR(20),
    province        VARCHAR(50),
    city            VARCHAR(50),
    district        VARCHAR(50),
    detail          VARCHAR(200),
    is_default      BOOLEAN DEFAULT false,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
