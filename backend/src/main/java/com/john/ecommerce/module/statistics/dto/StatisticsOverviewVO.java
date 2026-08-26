package com.john.ecommerce.module.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsOverviewVO {
    private Long orderCount;
    private Long gmv;
    private Long paidOrderCount;
    private List<SkuSalesVO> topSkus;
    private Long warehouseStockCount;
}
