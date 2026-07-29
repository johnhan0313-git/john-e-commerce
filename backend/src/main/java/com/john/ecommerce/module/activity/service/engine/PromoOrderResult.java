package com.john.ecommerce.module.activity.service.engine;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PromoOrderResult {
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal payAmount = BigDecimal.ZERO;
    private List<PromoLineResult> lines = new ArrayList<>();
    private List<PromoCandidate> applied = new ArrayList<>();

    @Data
    public static class PromoLineResult {
        private Long skuId;
        private Long spuId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private BigDecimal discountAmount;
        private BigDecimal payAmount;
        private Long activityId;
    }
}
