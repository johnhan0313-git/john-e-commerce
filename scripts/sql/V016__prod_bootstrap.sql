-- V016: production bootstrap (idempotent) — email admin + modules + default warehouse + MOCK pay

-- Schema patches
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS cancel_by BIGINT;
ALTER TABLE t_cart ADD COLUMN IF NOT EXISTS selected SMALLINT NOT NULL DEFAULT 1;

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_user_email
    ON t_user (email) WHERE delete_flag = 0 AND email IS NOT NULL;

-- Platform admin
UPDATE t_user
SET email = 'johnhan0313@gmail.com',
    nickname = '平台管理员',
    user_type = 1,
    status = 1,
    password_hash = NULL,
    updated_at = EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
WHERE id = 1;

INSERT INTO t_user (
    id, tenant_id, phone, email, nickname, user_type, status, password_hash,
    delete_flag, created_at, updated_at
) VALUES (
    1, 1, NULL, 'johnhan0313@gmail.com', '平台管理员', 1, 1,
    NULL, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;

-- Demo tenant (if missing)
INSERT INTO t_tenant (id, name, slug, business_types, status, config, delete_flag, created_at, updated_at)
VALUES (1, '演示租户', 'demo', '["B2C"]'::jsonb, 1, '{}'::jsonb, 0, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- Enable all product modules for demo tenant
INSERT INTO t_tenant_module (id, tenant_id, module_code, status, config, delete_flag, created_at, updated_at)
SELECT v.id, 1, v.code, 1, '{}'::jsonb, 0, 0, 0
FROM (VALUES
    (2001, 'tenant'),
    (2002, 'product'),
    (2003, 'trade'),
    (2004, 'payment'),
    (2005, 'content'),
    (2006, 'statistics'),
    (2007, 'activity'),
    (2008, 'fulfillment'),
    (2009, 'purchase'),
    (2010, 'ledger'),
    (2011, 'settle'),
    (2012, 'merchant'),
    (2013, 'crossborder')
) AS v(id, code)
WHERE NOT EXISTS (
    SELECT 1 FROM t_tenant_module tm
    WHERE tm.tenant_id = 1 AND tm.module_code = v.code AND tm.delete_flag = 0
);

UPDATE t_tenant_module
SET status = 1, expire_at = NULL, updated_at = EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
WHERE tenant_id = 1 AND delete_flag = 0
  AND module_code IN (
    'tenant','product','trade','payment','content','statistics',
    'activity','fulfillment','purchase','ledger','settle','merchant','crossborder'
  );

-- Default warehouse id=0 (OrderSplitter)
INSERT INTO t_warehouse (
    id, tenant_id, code, name, status, extra, delete_flag, created_at, updated_at
) VALUES (
    0, 1, 'DEFAULT', '默认仓', 1, '{}'::jsonb, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;

-- MOCK payment routing
INSERT INTO t_pay_account (
    id, tenant_id, account_code, name, owner_type, owner_id, currency, status,
    extra, delete_flag, created_at, updated_at
) VALUES (
    3001, 1, 'DEMO_PLATFORM', '演示平台收款户', 'PLATFORM', 1, 'CNY', 1,
    '{}'::jsonb, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_pay_channel_config (
    id, tenant_id, pay_account_id, channel_type, mch_no, credentials, capability,
    weight, status, extra, delete_flag, created_at, updated_at
) VALUES (
    3002, 1, 3001, 'MOCK', 'MOCK_MCH_001', '{}'::jsonb, '{}'::jsonb,
    100, 1, '{}'::jsonb, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_pay_method (
    id, tenant_id, method_code, name, icon_url, sort_order, status, extra,
    delete_flag, created_at, updated_at
) VALUES (
    3003, 1, 'MOCK', '模拟支付', NULL, 10, 1, '{}'::jsonb,
    0, 0, 0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_pay_route_rule (
    id, tenant_id, method_code, scene, pay_account_id, channel_type, condition,
    priority, status, delete_flag, created_at, updated_at
) VALUES (
    3004, 1, 'MOCK', 'DEFAULT', 3001, 'MOCK', '{}'::jsonb,
    100, 1, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;
