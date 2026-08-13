-- Refund ownership and item-level refund details.
ALTER TABLE t_refund ADD COLUMN IF NOT EXISTS user_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_t_refund_user ON t_refund (tenant_id, user_id);

CREATE TABLE IF NOT EXISTS t_refund_item (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    refund_id           BIGINT NOT NULL,
    order_item_id       BIGINT NOT NULL,
    sku_id              BIGINT NOT NULL,
    quantity            INT NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    stock_restored      SMALLINT NOT NULL DEFAULT 0,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_refund_item_order_item
    ON t_refund_item (tenant_id, refund_id, order_item_id) WHERE delete_flag = 0;
CREATE INDEX IF NOT EXISTS idx_t_refund_item_refund ON t_refund_item (tenant_id, refund_id);

