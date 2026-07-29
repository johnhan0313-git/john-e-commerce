-- V001: tenant & module registry

CREATE TABLE t_tenant (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(50) NOT NULL,
    business_types  JSONB NOT NULL DEFAULT '[]',
    status          SMALLINT NOT NULL DEFAULT 1,
    config          JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_tenant_slug ON t_tenant (slug) WHERE delete_flag = 0;

COMMENT ON TABLE t_tenant IS 'SaaS tenant master';

CREATE TABLE t_tenant_config (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    config_key      VARCHAR(100) NOT NULL,
    config_value    JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_tenant_config_key ON t_tenant_config (tenant_id, config_key) WHERE delete_flag = 0;
CREATE INDEX idx_t_tenant_config_tenant ON t_tenant_config (tenant_id);

COMMENT ON TABLE t_tenant_config IS 'Per-tenant key-value config (module-level settings)';

CREATE TABLE t_module_def (
    id              BIGINT PRIMARY KEY,
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    depends_on      JSONB NOT NULL DEFAULT '[]',
    default_enabled SMALLINT NOT NULL DEFAULT 0,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_module_def_code ON t_module_def (code) WHERE delete_flag = 0;

COMMENT ON TABLE t_module_def IS 'Platform module catalog';

CREATE TABLE t_tenant_module (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    module_code     VARCHAR(50) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    expire_at       BIGINT,
    config          JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_tenant_module ON t_tenant_module (tenant_id, module_code) WHERE delete_flag = 0;
CREATE INDEX idx_t_tenant_module_tenant ON t_tenant_module (tenant_id);
CREATE INDEX idx_t_tenant_module_code ON t_tenant_module (module_code);

COMMENT ON TABLE t_tenant_module IS 'Tenant module subscription / feature gate';

-- Seed platform modules (fixed ids for bootstrap)
INSERT INTO t_module_def (id, code, name, depends_on, default_enabled, delete_flag, created_at, updated_at) VALUES
(1001, 'tenant',       '租户基础',     '[]',                    1, 0, 0, 0),
(1002, 'product',      '商品',         '["tenant"]',            0, 0, 0, 0),
(1003, 'trade',        '交易订单',     '["product"]',           0, 0, 0, 0),
(1004, 'activity',     '营销活动',     '["product"]',           0, 0, 0, 0),
(1005, 'payment',      '支付',         '["trade"]',             0, 0, 0, 0),
(1006, 'ledger',       '账本',         '["payment"]',           0, 0, 0, 0),
(1007, 'settle',       '结算',         '["payment"]',           0, 0, 0, 0),
(1008, 'fulfillment',  '履约仓储',     '["product"]',           0, 0, 0, 0),
(1009, 'purchase',     '采购',         '["fulfillment"]',       0, 0, 0, 0),
(1010, 'merchant',     '多商家',       '["tenant"]',            0, 0, 0, 0),
(1011, 'content',      '内容装修',     '["tenant"]',            0, 0, 0, 0),
(1012, 'statistics',   '统计',         '["trade"]',             0, 0, 0, 0),
(1013, 'crossborder',  '跨境',         '["payment"]',           0, 0, 0, 0);
