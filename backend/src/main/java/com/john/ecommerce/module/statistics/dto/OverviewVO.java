package com.john.ecommerce.module.statistics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OverviewVO {
    /** order count grouped by status code */
    private Map<Integer, Long> orderCountByStatus;
    /** total GMV (sum of payAmount for non-cancelled orders) */
    private Long gmv;
    /** top SKUs by sold quantity */
    private List<TopSkuVO> topSkus;
    /** warehouse stock summary: total SKU count, total available */
    private StockSummaryVO stockSummary;

    @Data
    public static class TopSkuVO {
        private Long skuId;
        private String skuName;
        private Long totalQty;
    }

    @Data
    public static class StockSummaryVO {
        private Long skuCount;
        private Long totalAvailable;
        private Long totalLocked;
    }
}
