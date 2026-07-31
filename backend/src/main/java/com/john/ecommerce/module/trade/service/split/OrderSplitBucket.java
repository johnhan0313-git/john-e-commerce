package com.john.ecommerce.module.trade.service.split;

import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderSplitBucket {
    private Long merchantId;
    private Long shopId;
    private Long warehouseId;
    private String splitReason;
    private List<SplitLine> lines = new ArrayList<>();

    @Data
    public static class SplitLine {
        private Sku sku;
        private Spu spu;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal payAmount;
        private Long activityId;
    }
}
