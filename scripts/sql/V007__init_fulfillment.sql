-- V007: fulfillment, procurement, inventory & logistics

CREATE TABLE t_supplier (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(20),
    address         VARCHAR(300),
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_supplier_tenant ON t_supplier (tenant_id);

COMMENT ON TABLE t_supplier IS 'Procurement supplier master';

CREATE TABLE t_warehouse (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    merchant_id     BIGINT,
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    address         VARCHAR(300),
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_warehouse_code ON t_warehouse (tenant_id, code) WHERE delete_flag = 0;
CREATE INDEX idx_t_warehouse_tenant ON t_warehouse (tenant_id);
CREATE INDEX idx_t_warehouse_merchant ON t_warehouse (tenant_id, merchant_id);

COMMENT ON TABLE t_warehouse IS 'Warehouse master data';

CREATE TABLE t_purchase_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    po_no           VARCHAR(32) NOT NULL,
    supplier_id     BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    ref_activity_id BIGINT,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount    BIGINT NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    approved_at     BIGINT,
    finished_at     BIGINT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_purchase_order_no ON t_purchase_order (tenant_id, po_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_purchase_order_tenant ON t_purchase_order (tenant_id);
CREATE INDEX idx_t_purchase_order_supplier ON t_purchase_order (tenant_id, supplier_id);
CREATE INDEX idx_t_purchase_order_warehouse ON t_purchase_order (tenant_id, warehouse_id);

COMMENT ON TABLE t_purchase_order IS 'Purchase order (PO)';

CREATE TABLE t_purchase_order_item (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    purchase_order_id   BIGINT NOT NULL,
    sku_id              BIGINT NOT NULL,
    qty                 INT NOT NULL,
    received_qty        INT NOT NULL DEFAULT 0,
    price               BIGINT NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_purchase_order_item_tenant ON t_purchase_order_item (tenant_id);
CREATE INDEX idx_t_purchase_order_item_po ON t_purchase_order_item (tenant_id, purchase_order_id);
CREATE INDEX idx_t_purchase_order_item_sku ON t_purchase_order_item (tenant_id, sku_id);

COMMENT ON TABLE t_purchase_order_item IS 'Purchase order line';

CREATE TABLE t_warehouse_stock (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    available       INT NOT NULL DEFAULT 0,
    locked          INT NOT NULL DEFAULT 0,
    in_transit      INT NOT NULL DEFAULT 0,
    version         INT NOT NULL DEFAULT 0,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_warehouse_stock ON t_warehouse_stock (tenant_id, warehouse_id, sku_id) WHERE delete_flag = 0;
CREATE INDEX idx_t_warehouse_stock_tenant ON t_warehouse_stock (tenant_id);
CREATE INDEX idx_t_warehouse_stock_sku ON t_warehouse_stock (tenant_id, sku_id);

COMMENT ON TABLE t_warehouse_stock IS 'Warehouse x SKU stock summary';

CREATE TABLE t_stock_lot (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    lot_no          VARCHAR(64) NOT NULL,
    available       INT NOT NULL DEFAULT 0,
    locked          INT NOT NULL DEFAULT 0,
    in_transit      INT NOT NULL DEFAULT 0,
    version         INT NOT NULL DEFAULT 0,
    production_date BIGINT,
    expire_date     BIGINT,
    inbound_at      BIGINT,
    supplier_id     BIGINT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_stock_lot ON t_stock_lot (tenant_id, warehouse_id, sku_id, lot_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_stock_lot_tenant ON t_stock_lot (tenant_id);
CREATE INDEX idx_t_stock_lot_warehouse_sku ON t_stock_lot (tenant_id, warehouse_id, sku_id);
CREATE INDEX idx_t_stock_lot_expire ON t_stock_lot (tenant_id, warehouse_id, sku_id, expire_date);

COMMENT ON TABLE t_stock_lot IS 'Lot-level stock truth';

CREATE TABLE t_stock_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    stock_order_no  VARCHAR(32) NOT NULL,
    order_type      VARCHAR(20) NOT NULL,
    biz_type        VARCHAR(20) NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    ref_no          VARCHAR(64),
    status          SMALLINT NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    confirmed_at    BIGINT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_stock_order_no ON t_stock_order (tenant_id, stock_order_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_stock_order_tenant ON t_stock_order (tenant_id);
CREATE INDEX idx_t_stock_order_warehouse ON t_stock_order (tenant_id, warehouse_id);
CREATE INDEX idx_t_stock_order_ref ON t_stock_order (tenant_id, ref_no);

COMMENT ON TABLE t_stock_order IS 'Inbound/outbound/adjust stock document';

CREATE TABLE t_stock_order_item (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    stock_order_id  BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    qty             INT NOT NULL,
    actual_qty      INT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_stock_order_item_tenant ON t_stock_order_item (tenant_id);
CREATE INDEX idx_t_stock_order_item_order ON t_stock_order_item (tenant_id, stock_order_id);
CREATE INDEX idx_t_stock_order_item_sku ON t_stock_order_item (tenant_id, sku_id);

COMMENT ON TABLE t_stock_order_item IS 'Stock order line';

CREATE TABLE t_stock_order_lot (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    stock_order_item_id BIGINT NOT NULL,
    lot_id              BIGINT,
    lot_no              VARCHAR(64) NOT NULL,
    qty                 INT NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_stock_order_lot_tenant ON t_stock_order_lot (tenant_id);
CREATE INDEX idx_t_stock_order_lot_item ON t_stock_order_lot (tenant_id, stock_order_item_id);
CREATE INDEX idx_t_stock_order_lot_lot ON t_stock_order_lot (tenant_id, lot_id);

COMMENT ON TABLE t_stock_order_lot IS 'Stock order line lot allocation';

CREATE TABLE t_stock_lock_detail (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    order_item_id   BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    lot_id          BIGINT,
    lot_no          VARCHAR(64),
    qty             INT NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_stock_lock_detail_tenant ON t_stock_lock_detail (tenant_id);
CREATE INDEX idx_t_stock_lock_detail_order ON t_stock_lock_detail (tenant_id, order_id);
CREATE INDEX idx_t_stock_lock_detail_item ON t_stock_lock_detail (tenant_id, order_item_id);
CREATE INDEX idx_t_stock_lock_detail_lot ON t_stock_lock_detail (tenant_id, lot_id);

COMMENT ON TABLE t_stock_lock_detail IS 'Sales lock detail per lot';

CREATE TABLE t_stock_transfer (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    transfer_no         VARCHAR(32) NOT NULL,
    from_warehouse_id   BIGINT NOT NULL,
    to_warehouse_id     BIGINT NOT NULL,
    status              SMALLINT NOT NULL DEFAULT 0,
    remark              VARCHAR(500),
    shipped_at          BIGINT,
    received_at         BIGINT,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_stock_transfer_no ON t_stock_transfer (tenant_id, transfer_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_stock_transfer_tenant ON t_stock_transfer (tenant_id);
CREATE INDEX idx_t_stock_transfer_from ON t_stock_transfer (tenant_id, from_warehouse_id);
CREATE INDEX idx_t_stock_transfer_to ON t_stock_transfer (tenant_id, to_warehouse_id);

COMMENT ON TABLE t_stock_transfer IS 'Inter-warehouse transfer';

CREATE TABLE t_stock_transfer_item (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    transfer_id     BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    qty             INT NOT NULL,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_stock_transfer_item_tenant ON t_stock_transfer_item (tenant_id);
CREATE INDEX idx_t_stock_transfer_item_transfer ON t_stock_transfer_item (tenant_id, transfer_id);
CREATE INDEX idx_t_stock_transfer_item_sku ON t_stock_transfer_item (tenant_id, sku_id);

COMMENT ON TABLE t_stock_transfer_item IS 'Transfer line';

CREATE TABLE t_inventory_log (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    lot_no          VARCHAR(64),
    change_type     VARCHAR(30) NOT NULL,
    change_qty      INT NOT NULL,
    before_qty      INT NOT NULL,
    after_qty       INT NOT NULL,
    ref_type        VARCHAR(30),
    ref_id          BIGINT,
    remark          VARCHAR(200),
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_inventory_log_tenant ON t_inventory_log (tenant_id);
CREATE INDEX idx_t_inventory_log_warehouse_sku ON t_inventory_log (tenant_id, warehouse_id, sku_id);
CREATE INDEX idx_t_inventory_log_lot ON t_inventory_log (tenant_id, warehouse_id, sku_id, lot_no);
CREATE INDEX idx_t_inventory_log_ref ON t_inventory_log (tenant_id, ref_type, ref_id);

COMMENT ON TABLE t_inventory_log IS 'Inventory change audit trail';

CREATE TABLE t_logistics_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    order_id        BIGINT,
    logistics_no    VARCHAR(32) NOT NULL,
    provider        VARCHAR(30),
    tracking_no     VARCHAR(64),
    status          SMALLINT NOT NULL DEFAULT 0,
    shipped_at      BIGINT,
    delivered_at    BIGINT,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_logistics_tracking ON t_logistics_order (tenant_id, tracking_no) WHERE delete_flag = 0 AND tracking_no IS NOT NULL;
CREATE UNIQUE INDEX uk_t_logistics_no ON t_logistics_order (tenant_id, logistics_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_logistics_order_tenant ON t_logistics_order (tenant_id);
CREATE INDEX idx_t_logistics_order_order ON t_logistics_order (tenant_id, order_id);

COMMENT ON TABLE t_logistics_order IS 'Shipment / parcel';

CREATE TABLE t_logistics_item (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    logistics_id    BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    order_item_id   BIGINT NOT NULL,
    qty             INT NOT NULL,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_logistics_item_tenant ON t_logistics_item (tenant_id);
CREATE INDEX idx_t_logistics_item_logistics ON t_logistics_item (tenant_id, logistics_id);
CREATE INDEX idx_t_logistics_item_order ON t_logistics_item (tenant_id, order_id);
CREATE INDEX idx_t_logistics_item_order_item ON t_logistics_item (tenant_id, order_item_id);

COMMENT ON TABLE t_logistics_item IS 'Parcel line allocation (may span orders)';
