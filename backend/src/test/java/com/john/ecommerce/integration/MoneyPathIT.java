package com.john.ecommerce.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.module.payment.entity.SettlementOrder;
import com.john.ecommerce.module.payment.enums.PaymentStatus;
import com.john.ecommerce.module.payment.mapper.SettlementOrderMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Golden path: stock → order (lock) → MOCK pay/callback → settlement; cancel unlocks stock.
 */
class MoneyPathIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataSeeder seeder;
    @Autowired OrderMapper orderMapper;
    @Autowired SettlementOrderMapper settlementOrderMapper;

    String bearer;

    @BeforeEach
    void setUp() throws Exception {
        seeder.ensureCoreModules();
        seeder.ensureMockPaymentRouting();
        bearer = TestAuthHelper.loginAndBearer(mockMvc, objectMapper);
    }

    @Test
    void orderPayCallbackSettlesAndMarksPaid() throws Exception {
        TestDataSeeder.Catalog catalog = seeder.seedCatalogWithStock(10);

        JsonNode orderGroup = createOrder(catalog.skuId(), 2);
        Long orderId = orderGroup.path("orders").get(0).path("id").asLong();
        BigDecimal payAmount = orderGroup.path("orders").get(0).path("payAmount").decimalValue();

        assertThat(seeder.lockedQty(catalog.skuId())).isEqualTo(2);
        assertThat(seeder.availableQty(catalog.skuId())).isEqualTo(8);

        JsonNode payment = createMockPayment(orderId, payAmount);
        String payNo = payment.path("payNo").asText();
        assertThat(payment.path("status").asInt()).isEqualTo(PaymentStatus.PROCESSING.getCode());

        mockMvc.perform(post("/payment/mock-callback")
                        .header("Authorization", bearer)
                        .param("payNo", payNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
        try {
            Order order = orderMapper.selectById(orderId);
            assertThat(order.getPayStatus()).isEqualTo(PayStatus.PAID.getCode());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID.getCode());
            assertThat(order.getPaidAmount()).isEqualByComparingTo(payAmount);

            SettlementOrder so = settlementOrderMapper.selectOne(new LambdaQueryWrapper<SettlementOrder>()
                    .eq(SettlementOrder::getOrderId, orderId)
                    .eq(SettlementOrder::getPaymentId, payment.path("id").asLong())
                    .last("LIMIT 1"));
            assertThat(so).isNotNull();
            assertThat(so.getShopId()).isEqualTo(catalog.shopId());
            assertThat(so.getMerchantId()).isEqualTo(catalog.merchantId());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void cancelPendingOrderUnlocksStock() throws Exception {
        TestDataSeeder.Catalog catalog = seeder.seedCatalogWithStock(5);
        JsonNode orderGroup = createOrder(catalog.skuId(), 3);
        Long orderId = orderGroup.path("orders").get(0).path("id").asLong();
        assertThat(seeder.lockedQty(catalog.skuId())).isEqualTo(3);

        mockMvc.perform(put("/order/{id}/status", orderId)
                        .header("Authorization", bearer)
                        .param("status", String.valueOf(OrderStatus.CANCELLED.getCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertThat(seeder.lockedQty(catalog.skuId())).isZero();
        assertThat(seeder.availableQty(catalog.skuId())).isEqualTo(5);
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
                .andExpect(jsonPath("$.data.orders[0].id").exists())
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
                .andExpect(jsonPath("$.data.payNo").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
