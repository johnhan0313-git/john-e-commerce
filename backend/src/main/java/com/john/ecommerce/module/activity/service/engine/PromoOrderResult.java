package com.john.ecommerce.module.activity.service.engine;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PromoOrderResult {
    private Long totalAmount = 0L;
    private Long discountAmount = 0L;
    private Long payAmount = 0L;
    private List<PromoLineResult> lines = new ArrayList<>();
    private List<PromoCandidate> applied = new ArrayList<>();

    @Data
    public static class PromoLineResult {
        private Long skuId;
        private Long spuId;
        private int quantity;
        private Long unitPrice;
        private Long lineTotal;
        private Long discountAmount;
        private Long payAmount;
        private Long activityId;
    }
}
