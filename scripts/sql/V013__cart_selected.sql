-- Align cart checkbox column with Cart.selected / mall Cart.vue
ALTER TABLE t_cart RENAME COLUMN checked TO selected;
COMMENT ON COLUMN t_cart.selected IS '1=selected for checkout, 0=unchecked';
