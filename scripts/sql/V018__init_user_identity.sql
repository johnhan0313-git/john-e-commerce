-- V018: user multi-identity (buyer / seller / ops)

CREATE TABLE IF NOT EXISTS t_user_identity (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    identity_code   VARCHAR(32) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_user_identity_user_code
    ON t_user_identity (tenant_id, user_id, identity_code) WHERE delete_flag = 0;
CREATE INDEX IF NOT EXISTS idx_t_user_identity_user ON t_user_identity (tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_t_user_identity_code ON t_user_identity (tenant_id, identity_code);

COMMENT ON TABLE t_user_identity IS 'User multi-identity: buyer / seller / ops';
COMMENT ON COLUMN t_user_identity.identity_code IS 'buyer=买家 seller=卖家 ops=运营';
COMMENT ON COLUMN t_user_identity.status IS '1=启用 0=停用';

-- Backfill: every active user is a buyer
INSERT INTO t_user_identity (
    id, tenant_id, user_id, identity_code, status,
    delete_flag, created_at, updated_at
)
SELECT
    (u.id * 10 + 1),
    u.tenant_id,
    u.id,
    'buyer',
    1,
    0,
    COALESCE(u.created_at, 0),
    COALESCE(u.updated_at, 0)
FROM t_user u
WHERE u.delete_flag = 0
  AND NOT EXISTS (
        SELECT 1 FROM t_user_identity i
        WHERE i.user_id = u.id AND i.identity_code = 'buyer' AND i.delete_flag = 0
    );

-- Backfill: legacy user_type=1 → ops
INSERT INTO t_user_identity (
    id, tenant_id, user_id, identity_code, status,
    delete_flag, created_at, updated_at
)
SELECT
    (u.id * 10 + 2),
    u.tenant_id,
    u.id,
    'ops',
    1,
    0,
    COALESCE(u.created_at, 0),
    COALESCE(u.updated_at, 0)
FROM t_user u
WHERE u.delete_flag = 0 AND u.user_type = 1
  AND NOT EXISTS (
        SELECT 1 FROM t_user_identity i
        WHERE i.user_id = u.id AND i.identity_code = 'ops' AND i.delete_flag = 0
    );

-- Backfill: existing merchants → seller
INSERT INTO t_user_identity (
    id, tenant_id, user_id, identity_code, status,
    delete_flag, created_at, updated_at
)
SELECT
    (m.user_id * 10 + 3),
    m.tenant_id,
    m.user_id,
    'seller',
    1,
    0,
    COALESCE(m.created_at, 0),
    COALESCE(m.updated_at, 0)
FROM t_merchant m
WHERE m.delete_flag = 0
  AND NOT EXISTS (
        SELECT 1 FROM t_user_identity i
        WHERE i.user_id = m.user_id AND i.identity_code = 'seller' AND i.delete_flag = 0
    );
