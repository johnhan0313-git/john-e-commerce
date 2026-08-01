package com.john.ecommerce.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.module.fulfillment.entity.StockLot;
import com.john.ecommerce.module.fulfillment.entity.Warehouse;
import com.john.ecommerce.module.fulfillment.entity.WarehouseStock;
import com.john.ecommerce.module.fulfillment.mapper.StockLotMapper;
import com.john.ecommerce.module.fulfillment.mapper.WarehouseMapper;
import com.john.ecommerce.module.fulfillment.mapper.WarehouseStockMapper;
import com.john.ecommerce.module.payment.entity.PayAccount;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.PayMethod;
import com.john.ecommerce.module.payment.entity.PayRouteRule;
import com.john.ecommerce.module.payment.mapper.PayAccountMapper;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import com.john.ecommerce.module.payment.mapper.PayMethodMapper;
import com.john.ecommerce.module.payment.mapper.PayRouteRuleMapper;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import com.john.ecommerce.module.tenant.entity.TenantModule;
import com.john.ecommerce.module.tenant.mapper.TenantModuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Idempotent fixtures for golden-path integration tests (tenant 1 / warehouse 0).
 */
@Component
@RequiredArgsConstructor
public class TestDataSeeder {

    public static final long TENANT_ID = 1L;
    /** Matches {@link com.john.ecommerce.module.trade.service.split.OrderSplitter} default warehouse. */
    public static final long DEFAULT_WAREHOUSE_ID = 0L;

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100_000);

    private final TenantModuleMapper tenantModuleMapper;
    private final WarehouseMapper warehouseMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final StockLotMapper stockLotMapper;
    private final WarehouseStockMapper warehouseStockMapper;
    private final PayAccountMapper payAccountMapper;
    private final PayChannelConfigMapper payChannelConfigMapper;
    private final PayMethodMapper payMethodMapper;
    private final PayRouteRuleMapper payRouteRuleMapper;
    private final JdbcTemplate jdbcTemplate;

    public record Catalog(
            Long warehouseId,
            Long spuId,
            Long skuId,
            BigDecimal price,
            int stockQty,
            Long shopId,
            Long merchantId
    ) {}

    public void ensureModulesEnabled(String... moduleCodes) {
        withTenant(() -> {
            for (String code : moduleCodes) {
                TenantModule existing = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                        .eq(TenantModule::getModuleCode, code)
                        .last("LIMIT 1"));
                if (existing == null) {
                    // Avoid MP JSONB varchar cast issues on empty Map — use SQL cast
                    jdbcTemplate.update("""
                            INSERT INTO t_tenant_module
                              (id, tenant_id, module_code, status, config, delete_flag, created_at, updated_at)
                            SELECT ?, ?, ?, 1, '{}'::jsonb, 0, ?, ?
                            WHERE NOT EXISTS (
                              SELECT 1 FROM t_tenant_module
                              WHERE tenant_id = ? AND module_code = ? AND delete_flag = 0
                            )
                            """,
                            System.nanoTime(), TENANT_ID, code,
                            System.currentTimeMillis(), System.currentTimeMillis(),
                            TENANT_ID, code);
                } else if (existing.getStatus() == null || existing.getStatus() != 1) {
                    jdbcTemplate.update(
                            "UPDATE t_tenant_module SET status = 1, expire_at = NULL, updated_at = ? WHERE id = ? AND tenant_id = ?",
                            System.currentTimeMillis(), existing.getId(), TENANT_ID);
                }
            }
        });
    }

    public void ensureSchemaPatches() {
        // Idempotent patches for local DBs that may lag scripts/sql
        jdbcTemplate.execute("ALTER TABLE t_order ADD COLUMN IF NOT EXISTS cancel_by BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_spu ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_order ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_warehouse ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_settlement_order ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_settlement_bill ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE t_settlement ADD COLUMN IF NOT EXISTS shop_id BIGINT");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_shop (
                    id              BIGINT PRIMARY KEY,
                    tenant_id       BIGINT NOT NULL,
                    merchant_id     BIGINT NOT NULL,
                    name            VARCHAR(100) NOT NULL,
                    logo            VARCHAR(500),
                    status          SMALLINT NOT NULL DEFAULT 0,
                    extra           JSONB NOT NULL DEFAULT '{}',
                    delete_flag     SMALLINT NOT NULL DEFAULT 0,
                    created_at      BIGINT NOT NULL,
                    created_by      BIGINT,
                    updated_at      BIGINT NOT NULL,
                    updated_by      BIGINT,
                    idempotent_key  VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_t_user_email
                ON t_user (email) WHERE delete_flag = 0 AND email IS NOT NULL
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_user_identity (
                    id              BIGINT PRIMARY KEY,
                    tenant_id       BIGINT NOT NULL,
                    user_id         BIGINT NOT NULL,
                    identity_code   VARCHAR(32) NOT NULL,
                    status          SMALLINT NOT NULL DEFAULT 1,
                    extra           JSONB NOT NULL DEFAULT '{}',
                    delete_flag     SMALLINT NOT NULL DEFAULT 0,
                    created_at      BIGINT NOT NULL,
                    created_by      BIGINT,
                    updated_at      BIGINT NOT NULL,
                    updated_by      BIGINT,
                    idempotent_key  VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_t_user_identity_user_code
                ON t_user_identity (tenant_id, user_id, identity_code) WHERE delete_flag = 0
                """);
        jdbcTemplate.update("""
                UPDATE t_user SET email = 'johnhan0313@gmail.com', nickname = '平台管理员',
                  user_type = 1, status = 1, updated_at = 0
                WHERE id = 1
                """);
        // Demo admin: buyer + ops（同一邮箱可兼多身份）
        jdbcTemplate.update("""
                INSERT INTO t_user_identity (id, tenant_id, user_id, identity_code, status, delete_flag, created_at, updated_at)
                SELECT 11, 1, 1, 'buyer', 1, 0, 0, 0
                WHERE EXISTS (SELECT 1 FROM t_user WHERE id = 1)
                  AND NOT EXISTS (
                    SELECT 1 FROM t_user_identity WHERE user_id = 1 AND identity_code = 'buyer' AND delete_flag = 0
                  )
                """);
        jdbcTemplate.update("""
                INSERT INTO t_user_identity (id, tenant_id, user_id, identity_code, status, delete_flag, created_at, updated_at)
                SELECT 12, 1, 1, 'ops', 1, 0, 0, 0
                WHERE EXISTS (SELECT 1 FROM t_user WHERE id = 1)
                  AND NOT EXISTS (
                    SELECT 1 FROM t_user_identity WHERE user_id = 1 AND identity_code = 'ops' AND delete_flag = 0
                  )
                """);
    }

    public void ensureCoreModules() {
        ensureSchemaPatches();
        ensureModulesEnabled(
                ModuleCodes.TENANT,
                ModuleCodes.PRODUCT,
                ModuleCodes.TRADE,
                ModuleCodes.ACTIVITY,
                ModuleCodes.PAYMENT,
                ModuleCodes.LEDGER,
                ModuleCodes.SETTLE,
                ModuleCodes.FULFILLMENT,
                ModuleCodes.PURCHASE,
                ModuleCodes.CONTENT,
                ModuleCodes.STATISTICS
        );
    }

    public void setModuleStatus(String moduleCode, int status) {
        withTenant(() -> {
            TenantModule existing = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                    .eq(TenantModule::getModuleCode, moduleCode)
                    .last("LIMIT 1"));
            if (existing == null) {
                ensureModulesEnabled(moduleCode);
                existing = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                        .eq(TenantModule::getModuleCode, moduleCode)
                        .last("LIMIT 1"));
            }
            if (existing != null) {
                jdbcTemplate.update(
                        "UPDATE t_tenant_module SET status = ?, updated_at = ? WHERE id = ? AND tenant_id = ?",
                        status, System.currentTimeMillis(), existing.getId(), TENANT_ID);
            }
        });
    }

    public void ensureDefaultWarehouse() {
        withTenant(() -> {
            Warehouse wh = warehouseMapper.selectById(DEFAULT_WAREHOUSE_ID);
            if (wh == null) {
                long now = System.currentTimeMillis();
                jdbcTemplate.update("""
                        INSERT INTO t_warehouse
                          (id, tenant_id, code, name, status, extra, delete_flag, created_at, updated_at)
                        VALUES (?, ?, 'DEFAULT', '默认仓', 1, '{}'::jsonb, 0, ?, ?)
                        ON CONFLICT (id) DO NOTHING
                        """, DEFAULT_WAREHOUSE_ID, TENANT_ID, now, now);
            }
        });
    }

    public void ensureMockPaymentRouting() {
        withTenant(() -> {
            PayAccount account = payAccountMapper.selectOne(new LambdaQueryWrapper<PayAccount>()
                    .eq(PayAccount::getAccountCode, "DEMO_PLATFORM")
                    .last("LIMIT 1"));
            if (account == null) {
                account = new PayAccount();
                account.setAccountCode("DEMO_PLATFORM");
                account.setName("演示平台收款户");
                account.setOwnerType("PLATFORM");
                account.setOwnerId(1L);
                account.setCurrency("CNY");
                account.setStatus(1);
                account.setDeleteFlag(0);
                payAccountMapper.insert(account);
            }

            PayChannelConfig config = payChannelConfigMapper.selectOne(new LambdaQueryWrapper<PayChannelConfig>()
                    .eq(PayChannelConfig::getPayAccountId, account.getId())
                    .eq(PayChannelConfig::getChannelType, "MOCK")
                    .last("LIMIT 1"));
            if (config == null) {
                config = new PayChannelConfig();
                config.setPayAccountId(account.getId());
                config.setChannelType("MOCK");
                config.setMchNo("MOCK_MCH_001");
                config.setWeight(100);
                config.setStatus(1);
                config.setDeleteFlag(0);
                payChannelConfigMapper.insert(config);
            }

            PayMethod method = payMethodMapper.selectOne(new LambdaQueryWrapper<PayMethod>()
                    .eq(PayMethod::getMethodCode, "MOCK")
                    .last("LIMIT 1"));
            if (method == null) {
                method = new PayMethod();
                method.setMethodCode("MOCK");
                method.setName("模拟支付");
                method.setSortOrder(10);
                method.setStatus(1);
                method.setDeleteFlag(0);
                payMethodMapper.insert(method);
            }

            PayRouteRule rule = payRouteRuleMapper.selectOne(new LambdaQueryWrapper<PayRouteRule>()
                    .eq(PayRouteRule::getMethodCode, "MOCK")
                    .eq(PayRouteRule::getScene, "DEFAULT")
                    .last("LIMIT 1"));
            if (rule == null) {
                rule = new PayRouteRule();
                rule.setMethodCode("MOCK");
                rule.setScene("DEFAULT");
                rule.setPayAccountId(account.getId());
                rule.setChannelType("MOCK");
                rule.setPriority(100);
                rule.setStatus(1);
                rule.setDeleteFlag(0);
                payRouteRuleMapper.insert(rule);
            }
        });
    }

    public Catalog seedCatalogWithStock(int qty) {
        return withTenant(() -> {
            ensureDefaultWarehouse();
            long n = SEQ.incrementAndGet();
            long merchantId = 900_000L + n;
            long shopId = 910_000L + n;
            long now = System.currentTimeMillis();
            jdbcTemplate.update("""
                    INSERT INTO t_shop
                      (id, tenant_id, merchant_id, name, status, extra, delete_flag, created_at, updated_at)
                    VALUES (?, ?, ?, ?, 1, '{}'::jsonb, 0, ?, ?)
                    ON CONFLICT (id) DO NOTHING
                    """, shopId, TENANT_ID, merchantId, "测试店铺-" + n, now, now);

            Spu spu = new Spu();
            spu.setMerchantId(merchantId);
            spu.setShopId(shopId);
            spu.setProductCode("TEST-SPU-" + n);
            spu.setName("测试商品-" + n);
            spu.setProductType(0);
            spu.setStatus(1);
            spu.setSales(0);
            spu.setSortOrder(0);
            spu.setDeleteFlag(0);
            spuMapper.insert(spu);

            BigDecimal price = new BigDecimal("99.00");
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            sku.setSkuCode("TEST-SKU-" + n);
            sku.setSkuName("测试SKU-" + n);
            sku.setPrice(price);
            sku.setStatus(1);
            sku.setDeleteFlag(0);
            skuMapper.insert(sku);

            StockLot lot = new StockLot();
            lot.setWarehouseId(DEFAULT_WAREHOUSE_ID);
            lot.setSkuId(sku.getId());
            lot.setLotNo("LOT-" + n);
            lot.setAvailable(qty);
            lot.setLocked(0);
            lot.setInTransit(0);
            lot.setVersion(0);
            lot.setExpireDate(now + 86_400_000L * 30);
            lot.setInboundAt(now);
            lot.setDeleteFlag(0);
            stockLotMapper.insert(lot);

            WarehouseStock ws = warehouseStockMapper.selectOne(new LambdaQueryWrapper<WarehouseStock>()
                    .eq(WarehouseStock::getWarehouseId, DEFAULT_WAREHOUSE_ID)
                    .eq(WarehouseStock::getSkuId, sku.getId())
                    .last("LIMIT 1"));
            if (ws == null) {
                ws = new WarehouseStock();
                ws.setWarehouseId(DEFAULT_WAREHOUSE_ID);
                ws.setSkuId(sku.getId());
                ws.setAvailable(qty);
                ws.setLocked(0);
                ws.setInTransit(0);
                ws.setVersion(0);
                ws.setDeleteFlag(0);
                warehouseStockMapper.insert(ws);
            } else {
                ws.setAvailable(qty);
                ws.setLocked(0);
                warehouseStockMapper.updateById(ws);
            }

            return new Catalog(DEFAULT_WAREHOUSE_ID, spu.getId(), sku.getId(), price, qty, shopId, merchantId);
        });
    }

    public int lockedQty(Long skuId) {
        return withTenant(() -> {
            List<StockLot> lots = stockLotMapper.selectList(new LambdaQueryWrapper<StockLot>()
                    .eq(StockLot::getWarehouseId, DEFAULT_WAREHOUSE_ID)
                    .eq(StockLot::getSkuId, skuId));
            return lots.stream().mapToInt(l -> l.getLocked() != null ? l.getLocked() : 0).sum();
        });
    }

    public int availableQty(Long skuId) {
        return withTenant(() -> {
            List<StockLot> lots = stockLotMapper.selectList(new LambdaQueryWrapper<StockLot>()
                    .eq(StockLot::getWarehouseId, DEFAULT_WAREHOUSE_ID)
                    .eq(StockLot::getSkuId, skuId));
            return lots.stream().mapToInt(l -> l.getAvailable() != null ? l.getAvailable() : 0).sum();
        });
    }

    private void withTenant(Runnable action) {
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(TENANT_ID);
        try {
            action.run();
        } finally {
            if (prev == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prev);
            }
        }
    }

    private <T> T withTenant(java.util.concurrent.Callable<T> action) {
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(TENANT_ID);
        try {
            return action.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (prev == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prev);
            }
        }
    }
}
