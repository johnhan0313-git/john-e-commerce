package com.john.ecommerce.module.trade.service.split;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderSplitter {

    /**
     * 先按 shop 再按 warehouse 拆单；warehouse 暂用默认仓 0。
     * merchantId 一并带入桶内，写单时落库。
     */
    public List<OrderSplitBucket> split(List<OrderSplitBucket.SplitLine> lines) {
        Map<String, OrderSplitBucket> buckets = new LinkedHashMap<>();
        for (OrderSplitBucket.SplitLine line : lines) {
            Long shopId = line.getSpu().getShopId() != null ? line.getSpu().getShopId() : 0L;
            Long merchantId = line.getSpu().getMerchantId() != null ? line.getSpu().getMerchantId() : 0L;
            Long warehouseId = 0L;
            String key = shopId + ":" + warehouseId;
            OrderSplitBucket bucket = buckets.computeIfAbsent(key, k -> {
                OrderSplitBucket b = new OrderSplitBucket();
                b.setShopId(shopId);
                b.setMerchantId(merchantId);
                b.setWarehouseId(warehouseId);
                b.setSplitReason(shopId > 0 ? "SHOP" : "DEFAULT");
                return b;
            });
            bucket.getLines().add(line);
        }
        return new ArrayList<>(buckets.values());
    }
}
