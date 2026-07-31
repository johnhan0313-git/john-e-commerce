-- V015: email login — unique email + platform admin seed

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_user_email
    ON t_user (email) WHERE delete_flag = 0 AND email IS NOT NULL;

-- Platform ops admin (idempotent upsert by id=1)
UPDATE t_user
SET email = 'johnhan0313@gmail.com',
    nickname = '平台管理员',
    user_type = 1,
    status = 1,
    updated_at = 0
WHERE id = 1;

INSERT INTO t_user (
    id, tenant_id, phone, email, nickname, user_type, status, password_hash,
    delete_flag, created_at, updated_at
) VALUES (
    1, 1, NULL, 'johnhan0313@gmail.com', '平台管理员', 1, 1,
    NULL, 0, 0, 0
)
ON CONFLICT (id) DO NOTHING;
