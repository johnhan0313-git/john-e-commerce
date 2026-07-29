-- V003: product domain

CREATE TABLE t_brand (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    logo            VARCHAR(500),
    description     VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_brand_tenant ON t_brand (tenant_id);

COMMENT ON TABLE t_brand IS 'Product brand';

CREATE TABLE t_category (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(50) NOT NULL,
    icon_url        VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    level           SMALLINT NOT NULL DEFAULT 1,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_category_tenant ON t_category (tenant_id);
CREATE INDEX idx_t_category_parent ON t_category (tenant_id, parent_id);

COMMENT ON TABLE t_category IS 'Product category tree';

CREATE TABLE t_spu (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    merchant_id     BIGINT,
    category_id     BIGINT,
    brand_id        BIGINT,
    product_code    VARCHAR(64),
    name            VARCHAR(200) NOT NULL,
    subtitle        VARCHAR(500),
    main_images     JSONB,
    detail          TEXT,
    product_type    SMALLINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 0,
    sales           INT NOT NULL DEFAULT 0,
    sort_order      INT NOT NULL DEFAULT 0,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_spu_product_code ON t_spu (tenant_id, product_code) WHERE delete_flag = 0 AND product_code IS NOT NULL;
CREATE INDEX idx_t_spu_tenant ON t_spu (tenant_id);
CREATE INDEX idx_t_spu_category ON t_spu (tenant_id, category_id);
CREATE INDEX idx_t_spu_brand ON t_spu (tenant_id, brand_id);
CREATE INDEX idx_t_spu_merchant ON t_spu (tenant_id, merchant_id);

COMMENT ON TABLE t_spu IS 'Standard product unit (SPU)';

CREATE TABLE t_sku (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    spu_id          BIGINT NOT NULL,
    sku_code        VARCHAR(64),
    sku_name        VARCHAR(200),
    spec_values     JSONB,
    price           DECIMAL(12,2) NOT NULL,
    cost_price      DECIMAL(12,2),
    lot_enabled     SMALLINT NOT NULL DEFAULT 0,
    weight          DECIMAL(10,3),
    barcode         VARCHAR(50),
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_sku_code ON t_sku (tenant_id, sku_code) WHERE delete_flag = 0 AND sku_code IS NOT NULL;
CREATE INDEX idx_t_sku_tenant ON t_sku (tenant_id);
CREATE INDEX idx_t_sku_spu ON t_sku (tenant_id, spu_id);

COMMENT ON TABLE t_sku IS 'Stock keeping unit; inventory lives in warehouse tables';

CREATE TABLE t_price_rule (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    spu_id          BIGINT,
    sku_id          BIGINT,
    rule_type       VARCHAR(30) NOT NULL,
    min_qty         INT NOT NULL DEFAULT 1,
    price           DECIMAL(12,2) NOT NULL,
    start_time      BIGINT,
    end_time        BIGINT,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_price_rule_tenant ON t_price_rule (tenant_id);
CREATE INDEX idx_t_price_rule_spu ON t_price_rule (tenant_id, spu_id);
CREATE INDEX idx_t_price_rule_sku ON t_price_rule (tenant_id, sku_id);

COMMENT ON TABLE t_price_rule IS 'Tiered / time-bound price rules';

CREATE TABLE t_cart (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    checked         SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_cart_user_sku ON t_cart (tenant_id, user_id, sku_id) WHERE delete_flag = 0;
CREATE INDEX idx_t_cart_tenant ON t_cart (tenant_id);
CREATE INDEX idx_t_cart_user ON t_cart (tenant_id, user_id);

COMMENT ON TABLE t_cart IS 'Shopping cart line';
