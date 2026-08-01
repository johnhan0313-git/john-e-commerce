-- V008: payment, ledger, settlement & cross-border

-- ---------------------------------------------------------------------------
-- Pay account / channel / cashier / routing
-- ---------------------------------------------------------------------------

CREATE TABLE t_pay_route_policy (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    strategy_type   VARCHAR(30) NOT NULL,
    config          JSONB NOT NULL DEFAULT '{}',
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_pay_route_policy_tenant ON t_pay_route_policy (tenant_id);

COMMENT ON TABLE t_pay_route_policy IS 'Channel routing strategy (PRIORITY/WEIGHT_LB/FAIL_FAST/...)';

CREATE TABLE t_pay_account (
    id                      BIGINT PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    account_code            VARCHAR(50) NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    owner_type              VARCHAR(20) NOT NULL,
    owner_id                BIGINT,
    currency                VARCHAR(10) NOT NULL DEFAULT 'CNY',
    default_route_policy_id BIGINT,
    status                  SMALLINT NOT NULL DEFAULT 1,
    extra                   JSONB NOT NULL DEFAULT '{}',
    delete_flag             SMALLINT NOT NULL DEFAULT 0,
    created_at              BIGINT NOT NULL,
    created_by              BIGINT,
    updated_at              BIGINT NOT NULL,
    updated_by              BIGINT,
    idempotent_key          VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_pay_account_code ON t_pay_account (tenant_id, account_code) WHERE delete_flag = 0;
CREATE INDEX idx_t_pay_account_tenant ON t_pay_account (tenant_id);
CREATE INDEX idx_t_pay_account_owner ON t_pay_account (tenant_id, owner_type, owner_id);

COMMENT ON TABLE t_pay_account IS 'Payment subject account (tenant/platform/merchant)';

CREATE TABLE t_pay_channel_config (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    pay_account_id  BIGINT NOT NULL,
    channel_type    VARCHAR(30) NOT NULL,
    mch_no          VARCHAR(64) NOT NULL,
    credentials     JSONB NOT NULL DEFAULT '{}',
    capability      JSONB NOT NULL DEFAULT '{}',
    weight          INT NOT NULL DEFAULT 100,
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_pay_channel_config ON t_pay_channel_config (tenant_id, pay_account_id, channel_type, mch_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_pay_channel_config_tenant ON t_pay_channel_config (tenant_id);
CREATE INDEX idx_t_pay_channel_config_account ON t_pay_channel_config (tenant_id, pay_account_id);

COMMENT ON TABLE t_pay_channel_config IS 'Channel credentials under a pay account';

CREATE TABLE t_pay_method (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    method_code     VARCHAR(50) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    icon_url        VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_pay_method_code ON t_pay_method (tenant_id, method_code) WHERE delete_flag = 0;
CREATE INDEX idx_t_pay_method_tenant ON t_pay_method (tenant_id);

COMMENT ON TABLE t_pay_method IS 'Cashier display payment method';

CREATE TABLE t_pay_route_rule (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    method_code     VARCHAR(50) NOT NULL,
    scene           VARCHAR(30) NOT NULL DEFAULT 'DEFAULT',
    pay_account_id  BIGINT NOT NULL,
    channel_type    VARCHAR(30),
    condition       JSONB NOT NULL DEFAULT '{}',
    priority        INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_pay_route_rule_tenant ON t_pay_route_rule (tenant_id);
CREATE INDEX idx_t_pay_route_rule_method ON t_pay_route_rule (tenant_id, method_code, scene);
CREATE INDEX idx_t_pay_route_rule_account ON t_pay_route_rule (tenant_id, pay_account_id);

COMMENT ON TABLE t_pay_route_rule IS 'Method+scene to pay account routing rule';

-- ---------------------------------------------------------------------------
-- Payment documents
-- ---------------------------------------------------------------------------

CREATE TABLE t_payment (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    pay_no              VARCHAR(64) NOT NULL,
    method_code         VARCHAR(50) NOT NULL,
    pay_account_id      BIGINT,
    channel_config_id   BIGINT,
    channel_type        VARCHAR(30),
    currency            VARCHAR(10) NOT NULL DEFAULT 'CNY',
    parent_payment_id   BIGINT,
    order_group_no      VARCHAR(32),
    amount              DECIMAL(12,2) NOT NULL,
    status              SMALLINT NOT NULL DEFAULT 0,
    escrow_mode         VARCHAR(20),
    escrow_status       VARCHAR(20),
    freeze_flg          SMALLINT NOT NULL DEFAULT 0,
    route_trace         JSONB NOT NULL DEFAULT '[]',
    channel_trade_no    VARCHAR(100),
    paid_at             BIGINT,
    extra               JSONB NOT NULL DEFAULT '{}',
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_payment_pay_no ON t_payment (tenant_id, pay_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_payment_tenant ON t_payment (tenant_id);
CREATE INDEX idx_t_payment_group ON t_payment (tenant_id, order_group_no);
CREATE INDEX idx_t_payment_account ON t_payment (tenant_id, pay_account_id);
CREATE INDEX idx_t_payment_channel ON t_payment (tenant_id, channel_config_id);
CREATE INDEX idx_t_payment_parent ON t_payment (tenant_id, parent_payment_id);

COMMENT ON TABLE t_payment IS 'Payment document (may cover multiple orders)';

CREATE TABLE t_payment_item (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    payment_id      BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_payment_item_tenant ON t_payment_item (tenant_id);
CREATE INDEX idx_t_payment_item_payment ON t_payment_item (tenant_id, payment_id);
CREATE INDEX idx_t_payment_item_order ON t_payment_item (tenant_id, order_id);

COMMENT ON TABLE t_payment_item IS 'Payment allocation to sub-orders';

CREATE TABLE t_payment_fund_item (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    payment_id          BIGINT NOT NULL,
    fund_type           VARCHAR(20) NOT NULL,
    ledger_account_id   BIGINT,
    channel_config_id   BIGINT,
    amount              DECIMAL(12,2) NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_payment_fund_item_tenant ON t_payment_fund_item (tenant_id);
CREATE INDEX idx_t_payment_fund_item_payment ON t_payment_fund_item (tenant_id, payment_id);
CREATE INDEX idx_t_payment_fund_item_ledger ON t_payment_fund_item (tenant_id, ledger_account_id);

COMMENT ON TABLE t_payment_fund_item IS 'Combo payment fund source (ledger/channel)';

CREATE TABLE t_pay_plan (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    plan_no         VARCHAR(32) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    due_at          BIGINT,
    status          SMALLINT NOT NULL DEFAULT 0,
    paid_payment_id BIGINT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_pay_plan_no ON t_pay_plan (tenant_id, plan_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_pay_plan_tenant ON t_pay_plan (tenant_id);
CREATE INDEX idx_t_pay_plan_order ON t_pay_plan (tenant_id, order_id);

COMMENT ON TABLE t_pay_plan IS 'Installment / partial payment plan';

-- ---------------------------------------------------------------------------
-- Ledger (gateway account balances, cents)
-- ---------------------------------------------------------------------------

CREATE TABLE t_ledger_account (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    owner_type          VARCHAR(20) NOT NULL,
    owner_id            BIGINT NOT NULL,
    account_type        VARCHAR(30) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'CNY',
    balance             BIGINT NOT NULL DEFAULT 0,
    frozen              BIGINT NOT NULL DEFAULT 0,
    available           BIGINT NOT NULL DEFAULT 0,
    version             INT NOT NULL DEFAULT 0,
    channel_config_id   BIGINT,
    status              SMALLINT NOT NULL DEFAULT 1,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_ledger_account ON t_ledger_account (tenant_id, owner_type, owner_id, account_type, currency) WHERE delete_flag = 0;
CREATE INDEX idx_t_ledger_account_tenant ON t_ledger_account (tenant_id);
CREATE INDEX idx_t_ledger_account_owner ON t_ledger_account (tenant_id, owner_type, owner_id);

COMMENT ON TABLE t_ledger_account IS 'Gateway ledger account (amounts in cents)';

CREATE TABLE t_ledger_txn (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    txn_no          VARCHAR(64) NOT NULL,
    txn_type        VARCHAR(30) NOT NULL,
    amount          BIGINT NOT NULL,
    currency        VARCHAR(10) NOT NULL DEFAULT 'CNY',
    status          SMALLINT NOT NULL DEFAULT 0,
    biz_type        VARCHAR(30),
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

CREATE UNIQUE INDEX uk_t_ledger_txn_no ON t_ledger_txn (tenant_id, txn_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_ledger_txn_tenant ON t_ledger_txn (tenant_id);
CREATE INDEX idx_t_ledger_txn_ref ON t_ledger_txn (tenant_id, ref_type, ref_id);

COMMENT ON TABLE t_ledger_txn IS 'Ledger business transaction (idempotent)';

CREATE TABLE t_ledger_flow (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    ledger_account_id   BIGINT NOT NULL,
    txn_id              BIGINT NOT NULL,
    direction           VARCHAR(10) NOT NULL,
    amount              BIGINT NOT NULL,
    balance_before      BIGINT NOT NULL,
    balance_after       BIGINT NOT NULL,
    biz_type            VARCHAR(30),
    ref_type            VARCHAR(30),
    ref_id              BIGINT,
    remark              VARCHAR(200),
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_ledger_flow_tenant ON t_ledger_flow (tenant_id);
CREATE INDEX idx_t_ledger_flow_account ON t_ledger_flow (tenant_id, ledger_account_id);
CREATE INDEX idx_t_ledger_flow_txn ON t_ledger_flow (tenant_id, txn_id);

COMMENT ON TABLE t_ledger_flow IS 'Ledger account flow line (cents)';

-- ---------------------------------------------------------------------------
-- Settlement orders / bills / netting (cents)
-- ---------------------------------------------------------------------------

CREATE TABLE t_settlement_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    settlement_no   VARCHAR(64) NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    biz_type        VARCHAR(20) NOT NULL,
    payment_id      BIGINT,
    order_id        BIGINT,
    merchant_id     BIGINT,
    shop_id         BIGINT,
    amount          BIGINT NOT NULL,
    currency        VARCHAR(10) NOT NULL DEFAULT 'CNY',
    bill_status     SMALLINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 0,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_settlement_order_no ON t_settlement_order (tenant_id, settlement_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_settlement_order_tenant ON t_settlement_order (tenant_id);
CREATE INDEX idx_t_settlement_order_payment ON t_settlement_order (tenant_id, payment_id);
CREATE INDEX idx_t_settlement_order_order ON t_settlement_order (tenant_id, order_id);
CREATE INDEX idx_t_settlement_order_merchant ON t_settlement_order (tenant_id, merchant_id);
CREATE INDEX idx_t_settlement_order_shop ON t_settlement_order (tenant_id, shop_id);
CREATE INDEX idx_t_settlement_order_bill_status ON t_settlement_order (tenant_id, bill_status);

COMMENT ON TABLE t_settlement_order IS 'Forward/reverse settlement order (signed cents)';

CREATE TABLE t_settlement_order_item (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    settlement_order_id BIGINT NOT NULL,
    out_account_type    VARCHAR(30),
    out_account_id      BIGINT,
    in_account_type     VARCHAR(30),
    in_account_id       BIGINT,
    amount              BIGINT NOT NULL,
    fee_type            VARCHAR(30),
    trade_type          VARCHAR(30),
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_settlement_order_item_tenant ON t_settlement_order_item (tenant_id);
CREATE INDEX idx_t_settlement_order_item_order ON t_settlement_order_item (tenant_id, settlement_order_id);

COMMENT ON TABLE t_settlement_order_item IS 'Settlement order account line';

CREATE TABLE t_settlement_bill (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    bill_no             VARCHAR(64) NOT NULL,
    merchant_id         BIGINT,
    shop_id             BIGINT,
    payee_type          VARCHAR(20),
    payee_id            BIGINT,
    period_start        BIGINT,
    period_end          BIGINT,
    currency            VARCHAR(10) NOT NULL DEFAULT 'CNY',
    bill_amount         BIGINT NOT NULL DEFAULT 0,
    pre_settle_amount   BIGINT NOT NULL DEFAULT 0,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    settle_status       SMALLINT NOT NULL DEFAULT 0,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_settlement_bill_no ON t_settlement_bill (tenant_id, bill_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_settlement_bill_tenant ON t_settlement_bill (tenant_id);
CREATE INDEX idx_t_settlement_bill_merchant ON t_settlement_bill (tenant_id, merchant_id);
CREATE INDEX idx_t_settlement_bill_shop ON t_settlement_bill (tenant_id, shop_id);

COMMENT ON TABLE t_settlement_bill IS 'Settlement bill (pool of booked settlement orders)';

CREATE TABLE t_settlement_bill_detail (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    settlement_bill_id  BIGINT NOT NULL,
    summary_type        VARCHAR(30),
    amount              BIGINT NOT NULL,
    qty                 INT,
    remark              VARCHAR(200),
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE INDEX idx_t_settlement_bill_detail_tenant ON t_settlement_bill_detail (tenant_id);
CREATE INDEX idx_t_settlement_bill_detail_bill ON t_settlement_bill_detail (tenant_id, settlement_bill_id);

COMMENT ON TABLE t_settlement_bill_detail IS 'Settlement bill summary line';

CREATE TABLE t_settlement_bill_ref (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    settlement_bill_id  BIGINT NOT NULL,
    settlement_order_id BIGINT NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_settlement_bill_ref ON t_settlement_bill_ref (tenant_id, settlement_bill_id, settlement_order_id) WHERE delete_flag = 0;
CREATE INDEX idx_t_settlement_bill_ref_bill ON t_settlement_bill_ref (tenant_id, settlement_bill_id);
CREATE INDEX idx_t_settlement_bill_ref_order ON t_settlement_bill_ref (tenant_id, settlement_order_id);

COMMENT ON TABLE t_settlement_bill_ref IS 'Bill to settlement_order booking relation';

CREATE TABLE t_settlement (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    settle_no           VARCHAR(64) NOT NULL,
    settlement_bill_id  BIGINT,
    merchant_id         BIGINT,
    shop_id             BIGINT,
    net_amount          BIGINT NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'CNY',
    status              SMALLINT NOT NULL DEFAULT 0,
    settled_at          BIGINT,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_settlement_no ON t_settlement (tenant_id, settle_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_settlement_tenant ON t_settlement (tenant_id);
CREATE INDEX idx_t_settlement_bill ON t_settlement (tenant_id, settlement_bill_id);
CREATE INDEX idx_t_settlement_merchant ON t_settlement (tenant_id, merchant_id);
CREATE INDEX idx_t_settlement_shop ON t_settlement (tenant_id, shop_id);

COMMENT ON TABLE t_settlement IS 'Netted settlement document after bill pooling';

CREATE TABLE t_settlement_item (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    settlement_id   BIGINT NOT NULL,
    account_type    VARCHAR(30) NOT NULL,
    account_id      BIGINT NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    amount          BIGINT NOT NULL,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_settlement_item_tenant ON t_settlement_item (tenant_id);
CREATE INDEX idx_t_settlement_item_settlement ON t_settlement_item (tenant_id, settlement_id);

COMMENT ON TABLE t_settlement_item IS 'Settlement account-pair aggregation line';

CREATE TABLE t_settlement_ref (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    settlement_id       BIGINT NOT NULL,
    settlement_order_id BIGINT NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_settlement_ref ON t_settlement_ref (tenant_id, settlement_id, settlement_order_id) WHERE delete_flag = 0;
CREATE INDEX idx_t_settlement_ref_settlement ON t_settlement_ref (tenant_id, settlement_id);
CREATE INDEX idx_t_settlement_ref_order ON t_settlement_ref (tenant_id, settlement_order_id);

COMMENT ON TABLE t_settlement_ref IS 'Settlement to settlement_order netting relation';

-- ---------------------------------------------------------------------------
-- Channel split
-- ---------------------------------------------------------------------------

CREATE TABLE t_split_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    split_no        VARCHAR(64) NOT NULL,
    payment_id      BIGINT,
    settlement_id   BIGINT,
    channel_type    VARCHAR(30),
    channel_split_no VARCHAR(100),
    total_amount    BIGINT NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 0,
    confirmed_at    BIGINT,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_split_order_no ON t_split_order (tenant_id, split_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_split_order_tenant ON t_split_order (tenant_id);
CREATE INDEX idx_t_split_order_payment ON t_split_order (tenant_id, payment_id);
CREATE INDEX idx_t_split_order_settlement ON t_split_order (tenant_id, settlement_id);

COMMENT ON TABLE t_split_order IS 'Channel profit-sharing order';

CREATE TABLE t_split_detail (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    split_order_id  BIGINT NOT NULL,
    receiver_type   VARCHAR(20) NOT NULL,
    receiver_id     BIGINT,
    receiver_account VARCHAR(100),
    amount          BIGINT NOT NULL,
    description     VARCHAR(200),
    status          SMALLINT NOT NULL DEFAULT 0,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_split_detail_tenant ON t_split_detail (tenant_id);
CREATE INDEX idx_t_split_detail_order ON t_split_detail (tenant_id, split_order_id);

COMMENT ON TABLE t_split_detail IS 'Split receiver line';

-- ---------------------------------------------------------------------------
-- Cross-border
-- ---------------------------------------------------------------------------

CREATE TABLE t_customs_declaration (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    declaration_no  VARCHAR(64) NOT NULL,
    payment_id      BIGINT,
    order_id        BIGINT,
    customs_code    VARCHAR(50),
    status          SMALLINT NOT NULL DEFAULT 0,
    declared_at     BIGINT,
    channel_ref_no  VARCHAR(100),
    payload         JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_customs_declaration_no ON t_customs_declaration (tenant_id, declaration_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_customs_declaration_tenant ON t_customs_declaration (tenant_id);
CREATE INDEX idx_t_customs_declaration_payment ON t_customs_declaration (tenant_id, payment_id);
CREATE INDEX idx_t_customs_declaration_order ON t_customs_declaration (tenant_id, order_id);

COMMENT ON TABLE t_customs_declaration IS 'Cross-border customs declaration';

CREATE TABLE t_fx_order (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    fx_no           VARCHAR(64) NOT NULL,
    payment_id      BIGINT,
    order_id        BIGINT,
    sell_currency   VARCHAR(10) NOT NULL,
    buy_currency    VARCHAR(10) NOT NULL,
    sell_amount     DECIMAL(12,2) NOT NULL,
    buy_amount      DECIMAL(12,2),
    exchange_rate   DECIMAL(18,8),
    status          SMALLINT NOT NULL DEFAULT 0,
    channel_ref_no  VARCHAR(100),
    completed_at    BIGINT,
    extra           JSONB NOT NULL DEFAULT '{}',
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE UNIQUE INDEX uk_t_fx_order_no ON t_fx_order (tenant_id, fx_no) WHERE delete_flag = 0;
CREATE INDEX idx_t_fx_order_tenant ON t_fx_order (tenant_id);
CREATE INDEX idx_t_fx_order_payment ON t_fx_order (tenant_id, payment_id);
CREATE INDEX idx_t_fx_order_order ON t_fx_order (tenant_id, order_id);

COMMENT ON TABLE t_fx_order IS 'Foreign exchange order';
