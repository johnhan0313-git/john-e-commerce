-- V012: align t_module_def columns with ModuleDef entity (module_code/module_name/...)

-- Rename legacy columns from V001
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_module_def' AND column_name = 'code'
    ) THEN
        ALTER TABLE t_module_def RENAME COLUMN code TO module_code;
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_module_def' AND column_name = 'name'
    ) THEN
        ALTER TABLE t_module_def RENAME COLUMN name TO module_name;
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 't_module_def' AND column_name = 'depends_on'
    ) THEN
        ALTER TABLE t_module_def RENAME COLUMN depends_on TO dependencies;
    END IF;
END $$;

ALTER TABLE t_module_def ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE t_module_def ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE t_module_def ADD COLUMN IF NOT EXISTS status SMALLINT NOT NULL DEFAULT 1;
ALTER TABLE t_module_def ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Recreate unique index on renamed column
DROP INDEX IF EXISTS uk_t_module_def_code;
CREATE UNIQUE INDEX IF NOT EXISTS uk_t_module_def_module_code
    ON t_module_def (module_code) WHERE delete_flag = 0;

-- Backfill sort_order from bootstrap ids (1001+)
UPDATE t_module_def
SET sort_order = GREATEST(0, (id - 1001) * 10)
WHERE sort_order = 0 AND id BETWEEN 1001 AND 1099;

UPDATE t_module_def SET status = 1 WHERE status IS NULL;
