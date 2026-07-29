package com.john.ecommerce.module.activity.service.engine;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PromoContext {
    private Long userId;
    private Long tenantId;
    private List<PromoLine> lines = new ArrayList<>();

    @Data
    public static class PromoLine {
        private Long skuId;
        private Long spuId;
        private Long categoryId;
        private Long merchantId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
