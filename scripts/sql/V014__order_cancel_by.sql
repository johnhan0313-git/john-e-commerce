-- V014: align t_order with Order entity (cancel_by)

ALTER TABLE t_order ADD COLUMN IF NOT EXISTS cancel_by BIGINT;

COMMENT ON COLUMN t_order.cancel_by IS 'User id who cancelled the order';
