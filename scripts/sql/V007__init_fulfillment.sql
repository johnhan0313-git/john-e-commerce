CREATE TABLE t_inventory_log (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    change_qty      INT NOT NULL,
    before_stock    INT NOT NULL,
    after_stock     INT NOT NULL,
    change_type     VARCHAR(30) NOT NULL,
    ref_id          BIGINT,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_logistics_order (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    provider        VARCHAR(30),
    tracking_no     VARCHAR(64),
    status          SMALLINT NOT NULL DEFAULT 0,
    extra           JSONB DEFAULT '{}',
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
