package com.john.ecommerce.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.common.module.ModuleCodes;
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

    @BeforeEach
    void setUp() {
        seeder.ensureSchemaPatches();
        seeder.ensureModulesEnabled(
                ModuleCodes.TENANT,
                ModuleCodes.MERCHANT,
                ModuleCodes.PRODUCT,
                ModuleCodes.TRADE,
                ModuleCodes.FULFILLMENT,
                ModuleCodes.PAYMENT
        );
        seeder.ensureDefaultWarehouse();
    }

    @Test
    void applyAuditProductOrderShipAndRejectForeignOrder() throws Exception {
        String bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM t_user WHERE email = ? AND delete_flag = 0 LIMIT 1",
                Long.class, TestAuthHelper.DEMO_EMAIL);
        assertThat(userId).isNotNull();
        jdbcTemplate.update(
                "UPDATE t_shop SET delete_flag = 1 WHERE merchant_id IN (SELECT id FROM t_merchant WHERE user_id = ?)",
                userId);
        jdbcTemplate.update("UPDATE t_merchant SET delete_flag = 1 WHERE user_id = ?", userId);

        mockMvc.perform(get("/merchant/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value((Object) null));

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
                .andReturn();
        JsonNode meNode = objectMapper.readTree(meRes.getResponse().getContentAsString()).path("data");
        String shopIdText = meNode.path("shop").path("id").asText();
        assertThat(meNode.path("shop").path("id").asLong()).isPositive();

        String spuBody = """
                {"name":"本店商品","merchantId":999999,"subtitle":"s"}
                """;
        MvcResult spuRes = mockMvc.perform(post("/shop/products")
                        .header("Authorization", bearer)
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
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String skuBody = """
                {"spuId":%d,"skuName":"SKU-A","price":19.9,"status":1}
                """.formatted(spuId);
        MvcResult skuRes = mockMvc.perform(post("/shop/products/" + spuId + "/skus")
                        .header("Authorization", bearer)
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
        jdbcTemplate.update("""
                INSERT INTO t_warehouse_stock
                  (id, tenant_id, warehouse_id, sku_id, available, locked, in_transit, version,
                   delete_flag, created_at, updated_at)
                VALUES (?, 1, 0, ?, 100, 0, 0, 0, 0, ?, ?)
                """, lotId + 1, skuId, now, now);

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

        mockMvc.perform(get("/shop/orders").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        String shipBody = """
                {"orderId":%d,"provider":"SF","trackingNo":"SF%s","items":[{"orderItemId":%d,"qty":1}]}
                """.formatted(orderId, orderId, orderItemId);
        mockMvc.perform(post("/shop/orders/" + orderId + "/ship")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult foreign = mockMvc.perform(get("/shop/orders/999999991").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(foreign.getResponse().getContentAsString()).path("code").asInt())
                .isNotEqualTo(200);
    }
}
