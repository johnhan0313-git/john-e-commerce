CREATE TABLE t_campaign (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    campaign_type   VARCHAR(30) NOT NULL,
    title           VARCHAR(200),
    subtitle        VARCHAR(500),
    start_time      BIGINT NOT NULL,
    end_time        BIGINT NOT NULL,
    warm_up_time    BIGINT,
    status          SMALLINT NOT NULL DEFAULT 0,
    priority        INT DEFAULT 0,
    rule_config     JSONB NOT NULL DEFAULT '{}',
    budget          DECIMAL(12,2),
    used_budget     DECIMAL(12,2) DEFAULT 0,
    total_quota     INT,
    used_quota      INT DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_campaign_scope (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    campaign_id     BIGINT NOT NULL,
    scope_type      SMALLINT NOT NULL DEFAULT 0,
    spu_id          BIGINT,
    category_id     BIGINT,
    sku_id          BIGINT,
    activity_price  DECIMAL(10,2),
    extra_config    JSONB DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_campaign_participation (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    campaign_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    participate_type VARCHAR(30) NOT NULL,
    ref_order_id    BIGINT,
    ref_group_id    BIGINT,
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
