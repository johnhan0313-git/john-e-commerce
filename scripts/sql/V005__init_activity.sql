-- V005: marketing activity domain (replaces campaign)

CREATE TABLE t_activity (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    activity_type   VARCHAR(30) NOT NULL,
    title           VARCHAR(200),
    subtitle        VARCHAR(500),
    start_time      BIGINT NOT NULL,
    end_time        BIGINT NOT NULL,
    warm_up_time    BIGINT,
    status          SMALLINT NOT NULL DEFAULT 0,
    priority        INT NOT NULL DEFAULT 0,
    stack_group     VARCHAR(50),
    stackable       SMALLINT NOT NULL DEFAULT 0,
    promo_stage     VARCHAR(30),
    rule_config     JSONB NOT NULL DEFAULT '{}',
    budget          BIGINT,
    used_budget     BIGINT NOT NULL DEFAULT 0,
    total_quota     INT,
    used_quota      INT NOT NULL DEFAULT 0,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_activity_tenant ON t_activity (tenant_id);
CREATE INDEX idx_t_activity_type ON t_activity (tenant_id, activity_type);
CREATE INDEX idx_t_activity_time ON t_activity (tenant_id, start_time, end_time);
CREATE INDEX idx_t_activity_status ON t_activity (tenant_id, status);

COMMENT ON TABLE t_activity IS 'Marketing activity / promotion';

CREATE TABLE t_activity_scope (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    scope_type      SMALLINT NOT NULL DEFAULT 0,
    spu_id          BIGINT,
    category_id     BIGINT,
    sku_id          BIGINT,
    activity_price  BIGINT,
    stock_limit     INT,
    sold_qty        INT NOT NULL DEFAULT 0,
    extra_config    JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_activity_scope_tenant ON t_activity_scope (tenant_id);
CREATE INDEX idx_t_activity_scope_activity ON t_activity_scope (tenant_id, activity_id);
CREATE INDEX idx_t_activity_scope_spu ON t_activity_scope (tenant_id, spu_id);
CREATE INDEX idx_t_activity_scope_sku ON t_activity_scope (tenant_id, sku_id);

COMMENT ON TABLE t_activity_scope IS 'Activity applicable scope (SPU/category/SKU)';

CREATE TABLE t_activity_benefit (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    benefit_type    VARCHAR(30) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    expire_at       BIGINT,
    payload         JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_activity_benefit_tenant ON t_activity_benefit (tenant_id);
CREATE INDEX idx_t_activity_benefit_activity ON t_activity_benefit (tenant_id, activity_id);
CREATE INDEX idx_t_activity_benefit_user ON t_activity_benefit (tenant_id, user_id);

COMMENT ON TABLE t_activity_benefit IS 'Issued benefit (coupon, voucher, etc.)';

CREATE TABLE t_activity_session (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    leader_user_id  BIGINT NOT NULL,
    target_size     INT NOT NULL,
    joined_size     INT NOT NULL DEFAULT 1,
    status          SMALLINT NOT NULL DEFAULT 0,
    expire_at       BIGINT,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_activity_session_tenant ON t_activity_session (tenant_id);
CREATE INDEX idx_t_activity_session_activity ON t_activity_session (tenant_id, activity_id);
CREATE INDEX idx_t_activity_session_leader ON t_activity_session (tenant_id, leader_user_id);

COMMENT ON TABLE t_activity_session IS 'Group-buy / team session';

CREATE TABLE t_activity_record (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    order_id        BIGINT,
    benefit_id      BIGINT,
    session_id      BIGINT,
    record_type     VARCHAR(30) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_activity_record_tenant ON t_activity_record (tenant_id);
CREATE INDEX idx_t_activity_record_activity ON t_activity_record (tenant_id, activity_id);
CREATE INDEX idx_t_activity_record_user ON t_activity_record (tenant_id, user_id);
CREATE INDEX idx_t_activity_record_order ON t_activity_record (tenant_id, order_id);
CREATE INDEX idx_t_activity_record_session ON t_activity_record (tenant_id, session_id);

COMMENT ON TABLE t_activity_record IS 'Activity participation / redemption audit trail';
