-- V011: seed demo payment method + MOCK channel routing for local 联调

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

-- Optional: enable statistics for admin dashboard
INSERT INTO t_tenant_module (id, tenant_id, module_code, status, delete_flag, created_at, updated_at)
VALUES (2006, 1, 'statistics', 1, 0, 0, 0)
ON CONFLICT (id) DO NOTHING;
