package com.john.ecommerce.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.module.payment.entity.SettlementBill;
import com.john.ecommerce.module.payment.entity.SettlementOrder;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.enums.AccountType;
import com.john.ecommerce.module.payment.ledger.mapper.LedgerAccountMapper;
import com.john.ecommerce.module.payment.mapper.SettlementBillMapper;
import com.john.ecommerce.module.payment.mapper.SettlementOrderMapper;
import com.john.ecommerce.support.AbstractIntegrationTest;
import com.john.ecommerce.support.TestAuthHelper;
import com.john.ecommerce.support.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MerchantPortalIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataSeeder seeder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired SettlementOrderMapper settlementOrderMapper;
    @Autowired SettlementBillMapper settlementBillMapper;
    @Autowired LedgerAccountMapper ledgerAccountMapper;

    @BeforeEach
    void setUp() {
        seeder.ensureSchemaPatches();
        seeder.ensureModulesEnabled(
                ModuleCodes.TENANT,
                ModuleCodes.MERCHANT,
                ModuleCodes.PRODUCT,
                ModuleCodes.TRADE,
                ModuleCodes.FULFILLMENT,
                ModuleCodes.PAYMENT,
                ModuleCodes.SETTLE,
                ModuleCodes.LEDGER
        );
        seeder.ensureDefaultWarehouse();
        seeder.ensureMockPaymentRouting();
    }

    @Test
    void applyAuditProductOrderShipAndRejectForeignOrder() throws Exception {
        String bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper,
                TestAuthHelper.DEMO_EMAIL, TestAuthHelper.DEMO_CODE, "merchant");

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM t_user WHERE email = ? AND delete_flag = 0 LIMIT 1",
                Long.class, TestAuthHelper.DEMO_EMAIL);
        assertThat(userId).isNotNull();
        jdbcTemplate.update(
                "UPDATE t_shop SET delete_flag = 1 WHERE merchant_id IN (SELECT id FROM t_merchant WHERE user_id = ?)",
                userId);
        jdbcTemplate.update("UPDATE t_merchant SET delete_flag = 1 WHERE user_id = ?", userId);

        MvcResult emptyMe = mockMvc.perform(get("/merchant/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        // NON_NULL JSON omits null data
        JsonNode emptyData = objectMapper.readTree(emptyMe.getResponse().getContentAsString()).path("data");
        assertThat(emptyData.isMissingNode() || emptyData.isNull()).isTrue();

        String applyBody = """
                {"name":"测试卖家店","contactName":"张三","contactPhone":"13800000001","licenseNo":"LIC-1"}
                """;
        MvcResult applyRes = mockMvc.perform(post("/merchant/apply")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn();
        JsonNode applyNode = objectMapper.readTree(applyRes.getResponse().getContentAsString()).path("data");
        String merchantIdText = applyNode.path("id").asText();
        assertThat(applyNode.path("id").asLong()).isPositive();

        mockMvc.perform(put("/merchant/" + merchantIdText + "/audit")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));

        MvcResult meRes = mockMvc.perform(get("/merchant/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.merchant.status").value(1))
                .andExpect(jsonPath("$.data.currentShop.id").exists())
                .andExpect(jsonPath("$.data.shops.length()").value(1))
                .andReturn();
        JsonNode meNode = objectMapper.readTree(meRes.getResponse().getContentAsString()).path("data");
        String shopIdText = meNode.path("currentShop").path("id").asText();
        assertThat(meNode.path("currentShop").path("id").asLong()).isPositive();

        String spuBody = """
                {"name":"本店商品","merchantId":999999,"subtitle":"s"}
                """;
        MvcResult spuRes = mockMvc.perform(post("/shop/products")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spuBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode spuNode = objectMapper.readTree(spuRes.getResponse().getContentAsString()).path("data");
        assertThat(spuNode.path("shopId").asText()).isEqualTo(shopIdText);
        assertThat(spuNode.path("merchantId").asText()).isEqualTo(merchantIdText);
        long spuId = spuNode.path("id").asLong();

        mockMvc.perform(put("/shop/products/" + spuId + "/status")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String skuBody = """
                {"spuId":%d,"skuName":"SKU-A","price":19.9,"status":1}
                """.formatted(spuId);
        MvcResult skuRes = mockMvc.perform(post("/shop/products/" + spuId + "/skus")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skuBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        long skuId = objectMapper.readTree(skuRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        long now = System.currentTimeMillis();
        long lotId = System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO t_stock_lot
                  (id, tenant_id, warehouse_id, sku_id, lot_no, available, locked, in_transit, version,
                   expire_date, inbound_at, delete_flag, created_at, updated_at)
                VALUES (?, 1, 0, ?, ?, 100, 0, 0, 0, ?, ?, 0, ?, ?)
                """, lotId, skuId, "LOT-MP-" + skuId, now + 86_400_000L * 30, now, now, now);
        int updated = jdbcTemplate.update("""
                UPDATE t_warehouse_stock
                   SET available = 100, locked = 0, updated_at = ?
                 WHERE tenant_id = 1 AND warehouse_id = 0 AND sku_id = ? AND delete_flag = 0
                """, now, skuId);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO t_warehouse_stock
                      (id, tenant_id, warehouse_id, sku_id, available, locked, in_transit, version,
                       delete_flag, created_at, updated_at)
                    VALUES (?, 1, 0, ?, 100, 0, 0, 0, 0, ?, ?)
                    """, lotId + 1, skuId, now, now);
        }

        String orderBody = """
                {"items":[{"skuId":%d,"quantity":1}],"receiverName":"买家","receiverPhone":"139","receiverAddress":"地址"}
                """.formatted(skuId);
        MvcResult orderRes = mockMvc.perform(post("/order")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode orderNode = objectMapper.readTree(orderRes.getResponse().getContentAsString())
                .path("data").path("orders").get(0);
        assertThat(orderNode.path("shopId").asText()).isEqualTo(shopIdText);
        assertThat(orderNode.path("merchantId").asText()).isEqualTo(merchantIdText);
        long orderId = orderNode.path("id").asLong();
        long orderItemId = orderNode.path("items").get(0).path("id").asLong();

        mockMvc.perform(get("/shop/orders")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        String shipBody = """
                {"orderId":%d,"provider":"SF","trackingNo":"SF%s","items":[{"orderItemId":%d,"qty":1}]}
                """.formatted(orderId, orderId, orderItemId);
        mockMvc.perform(post("/shop/orders/" + orderId + "/ship")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult foreign = mockMvc.perform(get("/shop/orders/999999991")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", shopIdText))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(foreign.getResponse().getContentAsString()).path("code").asInt())
                .isNotEqualTo(200);
    }

    @Test
    void secondShopIsolationAndShopScopedSettlement() throws Exception {
        String bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper,
                TestAuthHelper.DEMO_EMAIL, TestAuthHelper.DEMO_CODE, "merchant");

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM t_user WHERE email = ? AND delete_flag = 0 LIMIT 1",
                Long.class, TestAuthHelper.DEMO_EMAIL);
        assertThat(userId).isNotNull();
        jdbcTemplate.update(
                "UPDATE t_shop SET delete_flag = 1 WHERE merchant_id IN (SELECT id FROM t_merchant WHERE user_id = ?)",
                userId);
        jdbcTemplate.update("UPDATE t_merchant SET delete_flag = 1 WHERE user_id = ?", userId);

        MvcResult applyRes = mockMvc.perform(post("/merchant/apply")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"多店卖家","contactName":"李四","contactPhone":"13800000002","licenseNo":"LIC-2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        long merchantId = objectMapper.readTree(applyRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(put("/merchant/" + merchantId + "/audit")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/merchant/me")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"多店卖家更新","contactName":"李四改","contactPhone":"13800000099","licenseNo":"LIC-2B"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.name").value("多店卖家更新"))
                .andExpect(jsonPath("$.data.contactPhone").value("13800000099"));

        MvcResult meRes = mockMvc.perform(get("/merchant/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchant.name").value("多店卖家更新"))
                .andExpect(jsonPath("$.data.shops.length()").value(1))
                .andReturn();
        long shopA = objectMapper.readTree(meRes.getResponse().getContentAsString())
                .path("data").path("currentShop").path("id").asLong();

        MvcResult shopBRes = mockMvc.perform(post("/shop/apply")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"第二家店"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn();
        long shopB = objectMapper.readTree(shopBRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(put("/shop/" + shopB + "/audit")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        mockMvc.perform(post("/shop/products")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", String.valueOf(shopA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"A店商品"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shopId").value(shopA));

        mockMvc.perform(get("/shop/products")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", String.valueOf(shopB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        // product + stock for shop B, then pay → settlement by shop
        MvcResult spuBRes = mockMvc.perform(post("/shop/products")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", String.valueOf(shopB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"B店商品"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long spuB = objectMapper.readTree(spuBRes.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(put("/shop/products/" + spuB + "/status")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", String.valueOf(shopB))
                        .param("status", "1"))
                .andExpect(status().isOk());
        MvcResult skuBRes = mockMvc.perform(post("/shop/products/" + spuB + "/skus")
                        .header("Authorization", bearer)
                        .header("X-Shop-Id", String.valueOf(shopB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"spuId":%d,"skuName":"SKU-B","price":10.00,"status":1}
                                """.formatted(spuB)))
                .andExpect(status().isOk())
                .andReturn();
        long skuB = objectMapper.readTree(skuBRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        long now = System.currentTimeMillis();
        long lotId = System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO t_stock_lot
                  (id, tenant_id, warehouse_id, sku_id, lot_no, available, locked, in_transit, version,
                   expire_date, inbound_at, delete_flag, created_at, updated_at)
                VALUES (?, 1, 0, ?, ?, 50, 0, 0, 0, ?, ?, 0, ?, ?)
                """, lotId, skuB, "LOT-B-" + skuB, now + 86_400_000L * 30, now, now, now);
        int updatedStock = jdbcTemplate.update("""
                UPDATE t_warehouse_stock
                   SET available = 50, locked = 0, updated_at = ?
                 WHERE tenant_id = 1 AND warehouse_id = 0 AND sku_id = ? AND delete_flag = 0
                """, now, skuB);
        if (updatedStock == 0) {
            jdbcTemplate.update("""
                    INSERT INTO t_warehouse_stock
                      (id, tenant_id, warehouse_id, sku_id, available, locked, in_transit, version,
                       delete_flag, created_at, updated_at)
                    VALUES (?, 1, 0, ?, 50, 0, 0, 0, 0, ?, ?)
                    """, lotId + 1, skuB, now, now);
        }

        MvcResult orderRes = mockMvc.perform(post("/order")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"skuId":%d,"quantity":1}],"receiverName":"买家","receiverPhone":"139","receiverAddress":"地址"}
                                """.formatted(skuB)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode order = objectMapper.readTree(orderRes.getResponse().getContentAsString())
                .path("data").path("orders").get(0);
        long orderId = order.path("id").asLong();
        long payAmount = order.path("payAmount").asLong();
        assertThat(order.path("shopId").asLong()).isEqualTo(shopB);

        MvcResult payRes = mockMvc.perform(post("/payment")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"methodCode":"MOCK","currency":"CNY","items":[{"orderId":%d,"amount":%d}]}
                                """.formatted(orderId, payAmount)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode payment = objectMapper.readTree(payRes.getResponse().getContentAsString()).path("data");
        String payNo = payment.path("payNo").asText();
        long paymentId = payment.path("id").asLong();

        mockMvc.perform(post("/payment/mock-callback")
                        .header("Authorization", bearer)
                        .param("payNo", payNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
        try {
            SettlementOrder so = settlementOrderMapper.selectOne(new LambdaQueryWrapper<SettlementOrder>()
                    .eq(SettlementOrder::getOrderId, orderId)
                    .eq(SettlementOrder::getPaymentId, paymentId)
                    .last("LIMIT 1"));
            assertThat(so).isNotNull();
            assertThat(so.getShopId()).isEqualTo(shopB);
            assertThat(so.getMerchantId()).isEqualTo(merchantId);

            SettlementBill billB = settlementBillMapper.selectOne(new LambdaQueryWrapper<SettlementBill>()
                    .eq(SettlementBill::getShopId, shopB)
                    .eq(SettlementBill::getSettleStatus, 0)
                    .last("LIMIT 1"));
            assertThat(billB).isNotNull();

            // cross-shop post must fail
            SettlementBill billA = new SettlementBill();
            billA.setBillNo("BILL-A-" + shopA);
            billA.setMerchantId(merchantId);
            billA.setShopId(shopA);
            billA.setPayeeType("SHOP");
            billA.setPayeeId(shopA);
            billA.setCurrency("CNY");
            billA.setBillAmount(0L);
            billA.setPreSettleAmount(0L);
            billA.setStatus("OPEN");
            billA.setSettleStatus(0);
            settlementBillMapper.insert(billA);

            MvcResult cross = mockMvc.perform(post("/settlement-bill/" + billA.getId() + "/post")
                            .header("Authorization", bearer)
                            .param("settlementOrderId", String.valueOf(so.getId())))
                    .andExpect(status().isOk())
                    .andReturn();
            assertThat(objectMapper.readTree(cross.getResponse().getContentAsString()).path("code").asInt())
                    .isNotEqualTo(200);

            mockMvc.perform(post("/settlement/bill/" + billB.getId() + "/settle")
                            .header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // MockMvc filters clear TenantContext; restore before mapper reads
            TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
            LedgerAccount account = ledgerAccountMapper.selectOne(new LambdaQueryWrapper<LedgerAccount>()
                    .eq(LedgerAccount::getOwnerType, "SHOP")
                    .eq(LedgerAccount::getOwnerId, shopB)
                    .eq(LedgerAccount::getAccountType, AccountType.SHOP_BALANCE.getCode())
                    .last("LIMIT 1"));
            assertThat(account).isNotNull();
            assertThat(account.getBalance()).isPositive();
        } finally {
            TenantContext.clear();
        }
    }
}
