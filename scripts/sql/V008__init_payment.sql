CREATE TABLE t_payment (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    pay_no          VARCHAR(64) NOT NULL,
    channel         VARCHAR(30) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 0,
    paid_at         BIGINT,
    channel_trade_no VARCHAR(100),
    extra           JSONB DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_settlement (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    merchant_id     BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    commission      DECIMAL(10,2) DEFAULT 0,
    settle_no       VARCHAR(64) NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 0,
    settled_at      BIGINT,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
