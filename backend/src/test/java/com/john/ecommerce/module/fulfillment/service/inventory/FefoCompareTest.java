package com.john.ecommerce.module.fulfillment.service.inventory;

import com.john.ecommerce.module.fulfillment.entity.StockLot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FefoCompareTest {

    @Test
    void earlierExpireComesFirst() {
        StockLot early = lot(100L, 1L);
        StockLot late = lot(200L, 1L);
        assertThat(InventoryService.compareFefo(early, late)).isNegative();
        assertThat(InventoryService.compareFefo(late, early)).isPositive();
    }

    @Test
    void nullExpireSortedLast() {
        StockLot dated = lot(100L, 1L);
        StockLot noExpire = lot(null, 1L);
        assertThat(InventoryService.compareFefo(dated, noExpire)).isNegative();
    }

    @Test
    void sameExpireUsesInboundAt() {
        StockLot firstIn = lot(100L, 10L);
        StockLot secondIn = lot(100L, 20L);
        List<StockLot> lots = new ArrayList<>(List.of(secondIn, firstIn));
        lots.sort(InventoryService::compareFefo);
        assertThat(lots).containsExactly(firstIn, secondIn);
    }

    private static StockLot lot(Long expire, Long inbound) {
        StockLot lot = new StockLot();
        lot.setExpireDate(expire);
        lot.setInboundAt(inbound);
        return lot;
    }
}
