package com.john.ecommerce.module.trade.service.split;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderSplitter {

    /**
     * 先按 merchant 再按 warehouse 拆单；warehouse 暂用默认仓 0。
     */
    public List<OrderSplitBucket> split(List<OrderSplitBucket.SplitLine> lines) {
        Map<String, OrderSplitBucket> buckets = new LinkedHashMap<>();
        for (OrderSplitBucket.SplitLine line : lines) {
            Long merchantId = line.getSpu().getMerchantId() != null ? line.getSpu().getMerchantId() : 0L;
            Long warehouseId = 0L;
            String key = merchantId + ":" + warehouseId;
            OrderSplitBucket bucket = buckets.computeIfAbsent(key, k -> {
                OrderSplitBucket b = new OrderSplitBucket();
                b.setMerchantId(merchantId);
                b.setWarehouseId(warehouseId);
                b.setSplitReason(merchantId > 0 ? "MERCHANT" : "DEFAULT");
                return b;
            });
            bucket.getLines().add(line);
        }
        return new ArrayList<>(buckets.values());
    }
}
