-- V004: trade domain

CREATE TABLE t_order (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    order_no            VARCHAR(32) NOT NULL,
    order_group_no      VARCHAR(32) NOT NULL,
    user_id             BIGINT NOT NULL,
    merchant_id         BIGINT,
    warehouse_id        BIGINT,
    order_type          SMALLINT NOT NULL,
    status              SMALLINT NOT NULL DEFAULT 0,
    split_reason        VARCHAR(50),
    total_amount        DECIMAL(12,2) NOT NULL,
    discount_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    pay_amount          DECIMAL(12,2) NOT NULL,
    paid_amount         DECIMAL(12,2) NOT NULL DEFAULT 0,
    pay_status          SMALLINT NOT NULL DEFAULT 0,
    pay_type            SMALLINT,
    pay_time            BIGINT,
    pay_no              VARCHAR(64),
    receiver_name       VARCHAR(50),
    receiver_phone      VARCHAR(20),
    receiver_address    VARCHAR(300),
    dining_type         SMALLINT,
    table_no            VARCHAR(20),
    dining_party_size   INT,
    dining_remark       VARCHAR(200),
    remark              VARCHAR(500),
    activity_id         BIGINT,
    distributor_id      BIGINT,
    cancel_reason       VARCHAR(200),
    cancel_time         BIGINT,
    complete_time       BIGINT,
    extra               JSONB NOT NULL DEFAULT '{}',
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_order_no ON t_order (tenant_id, order_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_order_tenant ON t_order (tenant_id);
CREATE INDEX idx_t_order_user ON t_order (tenant_id, user_id);
CREATE INDEX idx_t_order_group ON t_order (tenant_id, order_group_no);
CREATE INDEX idx_t_order_merchant ON t_order (tenant_id, merchant_id);
CREATE INDEX idx_t_order_warehouse ON t_order (tenant_id, warehouse_id);
CREATE INDEX idx_t_order_activity ON t_order (tenant_id, activity_id);

COMMENT ON TABLE t_order IS 'Fulfillment sub-order (split unit)';

CREATE TABLE t_order_item (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    order_id            BIGINT NOT NULL,
    spu_id              BIGINT NOT NULL,
    sku_id              BIGINT NOT NULL,
    sku_name            VARCHAR(200),
    sku_image           VARCHAR(500),
    spec_values         JSONB,
    price               DECIMAL(12,2) NOT NULL,
    quantity            INT NOT NULL,
    subtotal            DECIMAL(12,2) NOT NULL,
    discount_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    pay_amount          DECIMAL(12,2) NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_order_item_tenant ON t_order_item (tenant_id);
CREATE INDEX idx_t_order_item_order ON t_order_item (tenant_id, order_id);
CREATE INDEX idx_t_order_item_sku ON t_order_item (tenant_id, sku_id);

COMMENT ON TABLE t_order_item IS 'Order line item';

CREATE TABLE t_refund (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    order_id            BIGINT NOT NULL,
    payment_id          BIGINT,
    refund_no           VARCHAR(64) NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    reason              VARCHAR(500),
    status              SMALLINT NOT NULL DEFAULT 0,
    refunded_at         BIGINT,
    channel_refund_no   VARCHAR(100),
    extra               JSONB NOT NULL DEFAULT '{}',
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_refund_no ON t_refund (tenant_id, refund_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_refund_tenant ON t_refund (tenant_id);
CREATE INDEX idx_t_refund_order ON t_refund (tenant_id, order_id);
CREATE INDEX idx_t_refund_payment ON t_refund (tenant_id, payment_id);

COMMENT ON TABLE t_refund IS 'Order refund request';
