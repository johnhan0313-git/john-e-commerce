CREATE TABLE t_category (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    parent_id       BIGINT DEFAULT 0,
    name            VARCHAR(50) NOT NULL,
    sort_order      INT DEFAULT 0,
    level           SMALLINT NOT NULL DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_spu (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    merchant_id     BIGINT,
    category_id     BIGINT,
    name            VARCHAR(200) NOT NULL,
    subtitle        VARCHAR(500),
    main_images     JSONB,
    detail          TEXT,
    product_type    SMALLINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 0,
    sales           INT DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_sku (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    spu_id          BIGINT NOT NULL,
    sku_name        VARCHAR(200),
    spec_values     JSONB,
    price           DECIMAL(10,2) NOT NULL,
    cost_price      DECIMAL(10,2),
    stock           INT NOT NULL DEFAULT 0,
    weight          DECIMAL(8,2),
    barcode         VARCHAR(50),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_price_rule (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    spu_id          BIGINT,
    sku_id          BIGINT,
    rule_type       VARCHAR(30) NOT NULL,
    min_qty         INT DEFAULT 1,
    price           DECIMAL(10,2) NOT NULL,
    start_time      BIGINT,
    end_time        BIGINT,
    status          SMALLINT DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
