-- V019: unpaid order pay deadline for timeout cancel / unlock

ALTER TABLE t_order ADD COLUMN IF NOT EXISTS pay_deadline BIGINT;

COMMENT ON COLUMN t_order.pay_deadline IS 'Unpaid order payment deadline (epoch ms); past due may auto-cancel and unlock stock';

CREATE INDEX IF NOT EXISTS idx_t_order_pay_deadline
    ON t_order (tenant_id, status, pay_status, pay_deadline)
    WHERE delete_flag = 0 AND pay_deadline IS NOT NULL;
