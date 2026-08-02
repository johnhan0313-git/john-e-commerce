-- V017: shop (storefront) under merchant; business tables add shop_id

CREATE TABLE t_shop (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    merchant_id     BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    logo            VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 0,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_shop_tenant ON t_shop (tenant_id);
CREATE INDEX idx_t_shop_merchant ON t_shop (tenant_id, merchant_id);
CREATE INDEX idx_t_shop_status ON t_shop (tenant_id, status);

COMMENT ON TABLE t_shop IS 'Shop / storefront under a merchant';
COMMENT ON COLUMN t_shop.status IS '0=pending audit, 1=open, 2=rejected, 3=disabled';

ALTER TABLE t_spu ADD COLUMN IF NOT EXISTS shop_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_t_spu_shop ON t_spu (tenant_id, shop_id);

ALTER TABLE t_order ADD COLUMN IF NOT EXISTS shop_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_t_order_shop ON t_order (tenant_id, shop_id);

ALTER TABLE t_warehouse ADD COLUMN IF NOT EXISTS shop_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_t_warehouse_shop ON t_warehouse (tenant_id, shop_id);

-- Enable merchant (+ fulfillment for ship, settle for shop billing) on demo tenant
INSERT INTO t_tenant_module (id, tenant_id, module_code, status, delete_flag, created_at, updated_at)
VALUES
(2017, 1, 'merchant', 1, 0, 0, 0),
(2018, 1, 'fulfillment', 1, 0, 0, 0),
(2019, 1, 'settle', 1, 0, 0, 0),
(2020, 1, 'ledger', 1, 0, 0, 0)
ON CONFLICT (tenant_id, module_code) WHERE delete_flag = 0 DO NOTHING;
