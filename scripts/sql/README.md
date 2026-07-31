# SQL migrations

This project does **not** use Flyway or Liquibase. Schema changes are maintained as versioned SQL files under `scripts/sql/`.

## How to apply

1. Create an **empty** PostgreSQL database.
2. Run every `V00x__*.sql` file **in numeric order** on that database:
   - `V001__init_tenant.sql`
   - `V002__init_user.sql`
   - `V003__init_product.sql`
   - `V004__init_trade.sql`
   - `V005__init_activity.sql`
   - `V006__init_merchant.sql`
   - `V007__init_fulfillment.sql`
   - `V008__init_payment.sql`
   - `V009__init_content.sql`
   - `V010__seed_dev_admin.sql`（演示租户 + 管理员，本地开发用）
   - …中间版本见目录…
   - `V017__init_shop.sql`（店铺 t_shop；SPU/订单/仓增加 shop_id）

Example:

```bash
for f in scripts/sql/V*.sql; do
  psql "$DATABASE_URL" -f "$f"
done
```

## Conventions

- Primary keys are `BIGINT` (application-assigned snowflake IDs), not `BIGSERIAL`.
- All tables include `delete_flag`, audit columns (`created_at`, `updated_at`, `created_by`, `updated_by`, `idempotent_key`).
- Business tables include `tenant_id` (except `t_tenant` and `t_module_def`).
- Order-facing amounts use `DECIMAL(12,2)`; ledger/settlement amounts use `BIGINT` cents.

## One-shot updates

When the schema changes, edit or add scripts here and **re-apply on a fresh database** (or hand-write a delta migration for existing environments). There is no automatic migration runner in the backend yet.
