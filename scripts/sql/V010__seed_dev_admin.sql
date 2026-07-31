-- V010: local/dev bootstrap tenant + admin (idempotent)

-- Demo tenant
INSERT INTO t_tenant (id, name, slug, business_types, status, config, delete_flag, created_at, updated_at)
VALUES (1, '演示租户', 'demo', '["B2C"]'::jsonb, 1, '{}'::jsonb, 0, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- Enable core modules for demo tenant
INSERT INTO t_tenant_module (id, tenant_id, module_code, status, delete_flag, created_at, updated_at)
VALUES
(2001, 1, 'tenant',  1, 0, 0, 0),
(2002, 1, 'product', 1, 0, 0, 0),
(2003, 1, 'trade',   1, 0, 0, 0),
(2004, 1, 'payment', 1, 0, 0, 0),
(2005, 1, 'content', 1, 0, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- Admin: email johnhan0313@gmail.com (邮箱验证码登录；dev 固定码见 app.auth.fixed-code)
INSERT INTO t_user (
    id, tenant_id, phone, email, nickname, user_type, status, password_hash,
    delete_flag, created_at, updated_at
) VALUES (
    1, 1, NULL, 'johnhan0313@gmail.com', '平台管理员', 1, 1,
    NULL, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;
