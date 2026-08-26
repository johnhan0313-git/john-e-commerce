package com.john.ecommerce.module.trade.service.split;

import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSplitterTest {

    private final OrderSplitter splitter = new OrderSplitter();

    @Test
    void splitsByShopKeepingDefaultWarehouse() {
        OrderSplitBucket.SplitLine a = line(100L, 10L, 1L);
        OrderSplitBucket.SplitLine b = line(100L, 10L, 2L);
        OrderSplitBucket.SplitLine c = line(200L, 20L, 3L);

        List<OrderSplitBucket> buckets = splitter.split(List.of(a, b, c));

        assertThat(buckets).hasSize(2);
        assertThat(buckets.get(0).getShopId()).isEqualTo(100L);
        assertThat(buckets.get(0).getMerchantId()).isEqualTo(10L);
        assertThat(buckets.get(0).getWarehouseId()).isEqualTo(0L);
        assertThat(buckets.get(0).getLines()).hasSize(2);
        assertThat(buckets.get(0).getSplitReason()).isEqualTo("SHOP");

        assertThat(buckets.get(1).getShopId()).isEqualTo(200L);
        assertThat(buckets.get(1).getMerchantId()).isEqualTo(20L);
        assertThat(buckets.get(1).getLines()).hasSize(1);
    }

    @Test
    void nullShopGoesToDefaultBucket() {
        OrderSplitBucket.SplitLine line = line(null, null, 9L);
        List<OrderSplitBucket> buckets = splitter.split(List.of(line));
        assertThat(buckets).hasSize(1);
        assertThat(buckets.get(0).getShopId()).isEqualTo(0L);
        assertThat(buckets.get(0).getMerchantId()).isEqualTo(0L);
        assertThat(buckets.get(0).getSplitReason()).isEqualTo("DEFAULT");
    }

    private static OrderSplitBucket.SplitLine line(Long shopId, Long merchantId, Long skuId) {
        Spu spu = new Spu();
        spu.setId(skuId + 100);
        spu.setShopId(shopId);
        spu.setMerchantId(merchantId);
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setSpuId(spu.getId());
        sku.setPrice(1000L);

        OrderSplitBucket.SplitLine line = new OrderSplitBucket.SplitLine();
        line.setSpu(spu);
        line.setSku(sku);
        line.setQuantity(1);
        line.setUnitPrice(1000L);
        line.setDiscountAmount(0L);
        line.setPayAmount(1000L);
        return line;
    }
}
