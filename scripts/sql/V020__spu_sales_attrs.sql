-- SPU 销售规格定义（用于前端笛卡尔积生成 SKU）
ALTER TABLE t_spu ADD COLUMN IF NOT EXISTS sales_attrs JSONB;

COMMENT ON COLUMN t_spu.sales_attrs IS 'Sales attributes: [{name, values: string[]}]';
