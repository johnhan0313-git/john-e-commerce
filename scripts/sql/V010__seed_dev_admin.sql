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

-- Admin: phone 13800000000 / password admin123
-- password_hash = BCrypt (Spring BCryptPasswordEncoder)
INSERT INTO t_user (
    id, tenant_id, phone, email, nickname, user_type, status, password_hash,
    delete_flag, created_at, updated_at
) VALUES (
    1, 1, '13800000000', 'admin@demo.local', '演示管理员', 1, 1,
    '$2a$10$tkg2tH9kzNhADH9rtvY0.eSrohnJsMiv/TN4LaB2W5HnmiYdCqvjK',
    0, 0, 0
)
ON CONFLICT (id) DO NOTHING;
