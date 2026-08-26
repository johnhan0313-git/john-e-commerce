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
import com.john.ecommerce.module.trade.service.OrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Golden path: stock → order (lock) → MOCK pay/callback → consume; cancel / timeout unlocks.
 */
class MoneyPathIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataSeeder seeder;
    @Autowired OrderMapper orderMapper;
    @Autowired OrderService orderService;
    @Autowired SettlementOrderMapper settlementOrderMapper;
    @Autowired JdbcTemplate jdbcTemplate;

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
        Long payAmount = orderGroup.path("orders").get(0).path("payAmount").asLong();

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

        // 支付成功扣减锁定库存：available 不变，locked 归零
        assertThat(seeder.lockedQty(catalog.skuId())).isZero();
        assertThat(seeder.availableQty(catalog.skuId())).isEqualTo(8);

        TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
        try {
            Order order = orderMapper.selectById(orderId);
            assertThat(order.getPayStatus()).isEqualTo(PayStatus.PAID.getCode());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID.getCode());
            assertThat(order.getPaidAmount()).isEqualTo(payAmount);
            assertThat(order.getPayDeadline()).isNotNull();

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
    void unpaidTimeoutCancelsAndUnlocksStock() throws Exception {
        TestDataSeeder.Catalog catalog = seeder.seedCatalogWithStock(10);
        JsonNode orderGroup = createOrder(catalog.skuId(), 2);
        Long orderId = orderGroup.path("orders").get(0).path("id").asLong();
        assertThat(seeder.lockedQty(catalog.skuId())).isEqualTo(2);

        // 将支付截止时间拨到过去，触发超时关单
        jdbcTemplate.update(
                "UPDATE t_order SET pay_deadline = ? WHERE id = ?",
                System.currentTimeMillis() - 1000L, orderId);

        TenantContext.setTenantId(TestDataSeeder.TENANT_ID);
        try {
            int n = orderService.cancelExpiredUnpaidOrders(50);
            assertThat(n).isGreaterThanOrEqualTo(1);
            Order order = orderMapper.selectById(orderId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED.getCode());
            assertThat(order.getCancelReason()).isEqualTo("PAY_TIMEOUT");
        } finally {
            TenantContext.clear();
        }

        assertThat(seeder.lockedQty(catalog.skuId())).isZero();
        assertThat(seeder.availableQty(catalog.skuId())).isEqualTo(10);
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

    private JsonNode createMockPayment(Long orderId, Long amount) throws Exception {
        String body = """
                {
                  "methodCode":"MOCK",
                  "currency":"CNY",
                  "items":[{"orderId":%d,"amount":%d}]
                }
                """.formatted(orderId, amount);
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
