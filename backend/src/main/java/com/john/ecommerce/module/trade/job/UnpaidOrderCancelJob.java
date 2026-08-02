package com.john.ecommerce.module.trade.job;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.enums.OrderStatus;
import com.john.ecommerce.common.enums.PayStatus;
import com.john.ecommerce.module.trade.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Auto-cancel unpaid orders past pay_deadline and unlock stock (per tenant).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnpaidOrderCancelJob {

    private final OrderService orderService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelayString = "${app.trade.unpaid-cancel-delay-ms:60000}")
    public void cancelExpired() {
        long now = System.currentTimeMillis();
        List<Long> tenantIds = jdbcTemplate.queryForList("""
                SELECT DISTINCT tenant_id FROM t_order
                 WHERE status = ? AND pay_status = ?
                   AND pay_deadline IS NOT NULL AND pay_deadline < ?
                   AND delete_flag = 0
                """, Long.class,
                OrderStatus.PENDING.getCode(),
                PayStatus.UNPAID.getCode(),
                now);
        int total = 0;
        for (Long tenantId : tenantIds) {
            if (tenantId == null) continue;
            TenantContext.setTenantId(tenantId);
            try {
                total += orderService.cancelExpiredUnpaidOrders(100);
            } finally {
                TenantContext.clear();
            }
        }
        if (total > 0) {
            log.info("UnpaidOrderCancelJob cancelled {} expired unpaid order(s) across {} tenant(s)",
                    total, tenantIds.size());
        }
    }
}
