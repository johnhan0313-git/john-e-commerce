-- Align cart checkbox column with Cart.selected / mall Cart.vue
-- Fresh installs (V003) already have selected; legacy DBs may still have checked.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_cart' AND column_name = 'checked'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_cart' AND column_name = 'selected'
    ) THEN
        ALTER TABLE t_cart RENAME COLUMN checked TO selected;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_cart' AND column_name = 'selected'
    ) THEN
        ALTER TABLE t_cart ADD COLUMN selected SMALLINT NOT NULL DEFAULT 1;
    END IF;
END $$;

COMMENT ON COLUMN t_cart.selected IS '1=selected for checkout, 0=unchecked';
