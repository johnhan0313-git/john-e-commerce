package com.john.ecommerce.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.support.AbstractIntegrationTest;
import com.john.ecommerce.support.TestAuthHelper;
import com.john.ecommerce.support.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FulfillmentAndFeatureGateIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataSeeder seeder;
    @Autowired OrderMapper orderMapper;

    String bearer;

    @BeforeEach
    void setUp() throws Exception {
        seeder.ensureCoreModules();
        seeder.ensureMockPaymentRouting();
        bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper);
    }

    @Test
    void shipDeliverAndConfirmReceipt() throws Exception {
        TestDataSeeder.Catalog catalog = seeder.seedCatalogWithStock(4);
        JsonNode orderGroup = createOrder(catalog.skuId(), 1);
        Long orderId = orderGroup.path("orders").get(0).path("id").asLong();
        Long orderItemId = orderGroup.path("orders").get(0).path("items").get(0).path("id").asLong();
        BigDecimal payAmount = orderGroup.path("orders").get(0).path("payAmount").decimalValue();

        JsonNode payment = createMockPayment(orderId, payAmount);
        mockMvc.perform(post("/payment/mock-callback")
                        .header("Authorization", bearer)
                        .param("payNo", payment.path("payNo").asText()))
                .andExpect(status().isOk());

        String tracking = "TRK-" + System.nanoTime();
        String shipBody = """
                {
                  "orderId":%d,
                  "provider":"SF",
                  "trackingNo":"%s",
                  "items":[{"orderItemId":%d,"qty":1}]
                }
                """.formatted(orderId, tracking, orderItemId);
        mockMvc.perform(post("/logistics")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertOrderStatus(orderId, OrderStatus.SHIPPED);

        mockMvc.perform(post("/logistics/webhook/{trackingNo}", tracking)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        assertOrderStatus(orderId, OrderStatus.DELIVERED);

        mockMvc.perform(put("/logistics/confirm-receipt/{orderId}", orderId)
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    @Test
    void featureGateBlocksDisabledModule() throws Exception {
        seeder.setModuleStatus(ModuleCodes.STATISTICS, 0);
        try {
            mockMvc.perform(get("/statistics/overview").header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("statistics")));
        } finally {
            seeder.setModuleStatus(ModuleCodes.STATISTICS, 1);
        }

        mockMvc.perform(get("/statistics/overview").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private void assertOrderStatus(Long orderId, OrderStatus expected) {
        TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
        try {
            Order order = orderMapper.selectById(orderId);
            assertThat(order.getStatus()).isEqualTo(expected.getCode());
        } finally {
            TenantContext.clear();
        }
    }

    private JsonNode createOrder(Long skuId, int qty) throws Exception {
        String body = """
                {
                  "items":[{"skuId":%d,"quantity":%d}],
                  "receiverName":"测试",
                  "receiverPhone":"13900000000",
                  "receiverAddress":"测试地址"
                }
                """.formatted(skuId, qty);
        MvcResult result = mockMvc.perform(post("/order")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private JsonNode createMockPayment(Long orderId, BigDecimal amount) throws Exception {
        String body = """
                {
                  "methodCode":"MOCK",
                  "currency":"CNY",
                  "items":[{"orderId":%d,"amount":%s}]
                }
                """.formatted(orderId, amount.toPlainString());
        MvcResult result = mockMvc.perform(post("/payment")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
