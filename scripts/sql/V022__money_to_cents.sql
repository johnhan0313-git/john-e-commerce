-- Unify business amounts to BIGINT cents (no historical compatibility).
-- Safe on empty/dev DBs; multiplies existing DECIMAL yuan by 100 when converting.

ALTER TABLE t_sku
    ALTER COLUMN price TYPE BIGINT USING ROUND(price * 100)::BIGINT,
    ALTER COLUMN cost_price TYPE BIGINT USING CASE WHEN cost_price IS NULL THEN NULL ELSE ROUND(cost_price * 100)::BIGINT END;

ALTER TABLE t_price_rule
    ALTER COLUMN price TYPE BIGINT USING ROUND(price * 100)::BIGINT;

ALTER TABLE t_order
    ALTER COLUMN total_amount TYPE BIGINT USING ROUND(total_amount * 100)::BIGINT,
    ALTER COLUMN discount_amount TYPE BIGINT USING ROUND(discount_amount * 100)::BIGINT,
    ALTER COLUMN pay_amount TYPE BIGINT USING ROUND(pay_amount * 100)::BIGINT,
    ALTER COLUMN paid_amount TYPE BIGINT USING ROUND(paid_amount * 100)::BIGINT;

ALTER TABLE t_order_item
    ALTER COLUMN price TYPE BIGINT USING ROUND(price * 100)::BIGINT,
    ALTER COLUMN subtotal TYPE BIGINT USING ROUND(subtotal * 100)::BIGINT,
    ALTER COLUMN discount_amount TYPE BIGINT USING ROUND(discount_amount * 100)::BIGINT,
    ALTER COLUMN pay_amount TYPE BIGINT USING ROUND(pay_amount * 100)::BIGINT;

ALTER TABLE t_refund
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_refund_item
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_activity
    ALTER COLUMN budget TYPE BIGINT USING CASE WHEN budget IS NULL THEN NULL ELSE ROUND(budget * 100)::BIGINT END,
    ALTER COLUMN used_budget TYPE BIGINT USING ROUND(used_budget * 100)::BIGINT;

ALTER TABLE t_activity_scope
    ALTER COLUMN activity_price TYPE BIGINT USING CASE WHEN activity_price IS NULL THEN NULL ELSE ROUND(activity_price * 100)::BIGINT END;

ALTER TABLE t_purchase_order
    ALTER COLUMN total_amount TYPE BIGINT USING ROUND(total_amount * 100)::BIGINT;

ALTER TABLE t_purchase_order_item
    ALTER COLUMN price TYPE BIGINT USING ROUND(price * 100)::BIGINT;

ALTER TABLE t_payment
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_payment_item
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_payment_fund_item
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_pay_plan
    ALTER COLUMN amount TYPE BIGINT USING ROUND(amount * 100)::BIGINT;

ALTER TABLE t_fx_order
    ALTER COLUMN sell_amount TYPE BIGINT USING ROUND(sell_amount * 100)::BIGINT,
    ALTER COLUMN buy_amount TYPE BIGINT USING CASE WHEN buy_amount IS NULL THEN NULL ELSE ROUND(buy_amount * 100)::BIGINT END;
