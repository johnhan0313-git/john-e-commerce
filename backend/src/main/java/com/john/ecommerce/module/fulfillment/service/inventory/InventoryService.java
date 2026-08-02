package com.john.ecommerce.module.fulfillment.service.inventory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.entity.*;
import com.john.ecommerce.module.fulfillment.mapper.*;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real inventory implementation replacing NoOpInventoryFacade.
 * FEFO/FIFO allocation from StockLot; DEFAULT lot for non-lot-enabled SKUs.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class InventoryService implements InventoryFacade {

    private static final String DEFAULT_LOT = "DEFAULT";

    private final WarehouseStockMapper warehouseStockMapper;
    private final StockLotMapper stockLotMapper;
    private final StockLockDetailMapper stockLockDetailMapper;
    private final InventoryLogMapper inventoryLogMapper;

    @Override
    @Transactional
    public void lockForOrder(Order order, List<OrderItem> items) {
        Long warehouseId = order.getWarehouseId();
        if (warehouseId == null) return; // no warehouse assigned yet

        for (OrderItem item : items) {
            int remaining = item.getQuantity();
            List<StockLot> lots = selectAllocatableLots(warehouseId, item.getSkuId());
            if (lots.isEmpty()) {
                ensureDefaultLot(warehouseId, item.getSkuId());
                lots = selectAllocatableLots(warehouseId, item.getSkuId());
            }

            for (StockLot lot : lots) {
                if (remaining <= 0) break;
                int alloc = Math.min(remaining, lot.getAvailable());
                if (alloc <= 0) continue;

                int beforeLot = lot.getAvailable();
                lot.setAvailable(lot.getAvailable() - alloc);
                lot.setLocked(lot.getLocked() + alloc);
                stockLotMapper.updateById(lot);

                StockLockDetail detail = new StockLockDetail();
                detail.setOrderId(order.getId());
                detail.setOrderItemId(item.getId());
                detail.setWarehouseId(warehouseId);
                detail.setSkuId(item.getSkuId());
                detail.setLotId(lot.getId());
                detail.setLotNo(lot.getLotNo());
                detail.setQty(alloc);
                detail.setStatus(LOCK_STATUS_LOCKED);
                stockLockDetailMapper.insert(detail);

                writeLog(warehouseId, item.getSkuId(), lot.getLotNo(), "LOCK", -alloc,
                        beforeLot, lot.getAvailable(), "ORDER", order.getId(), null);
                remaining -= alloc;
            }
            if (remaining > 0) {
                throw new BizException("库存不足: skuId=" + item.getSkuId() + " 缺少" + remaining);
            }
            updateSummary(warehouseId, item.getSkuId());
        }
    }

    @Override
    @Transactional
    public void ensureStock(Long warehouseId, Long skuId, int minAvailable) {
        if (warehouseId == null || skuId == null || minAvailable <= 0) return;
        StockLot lot = ensureDefaultLot(warehouseId, skuId);
        int before = lot.getAvailable() != null ? lot.getAvailable() : 0;
        if (before >= minAvailable) {
            updateSummary(warehouseId, skuId);
            return;
        }
        int add = minAvailable - before;
        lot.setAvailable(before + add);
        stockLotMapper.updateById(lot);
        writeLog(warehouseId, skuId, lot.getLotNo(), "IN", add, before, lot.getAvailable(),
                "SKU_INIT", skuId, "ensureStock");
        updateSummary(warehouseId, skuId);
    }

    @Override
    @Transactional
    public void initOrSetAvailable(Long warehouseId, Long skuId, int qty) {
        if (warehouseId == null || skuId == null) return;
        if (qty < 0) throw new BizException("初始库存不能为负");
        StockLot lot = ensureDefaultLot(warehouseId, skuId);
        int before = lot.getAvailable() != null ? lot.getAvailable() : 0;
        int locked = lot.getLocked() != null ? lot.getLocked() : 0;
        if (locked > 0 && qty < before) {
            throw new BizException("存在锁定库存，无法下调可售");
        }
        if (before == qty) {
            updateSummary(warehouseId, skuId);
            return;
        }
        lot.setAvailable(qty);
        stockLotMapper.updateById(lot);
        int delta = qty - before;
        writeLog(warehouseId, skuId, lot.getLotNo(), delta >= 0 ? "IN" : "ADJUST", delta,
                before, qty, "SKU_INIT", skuId, "initOrSetAvailable");
        updateSummary(warehouseId, skuId);
    }

    @Override
    public int getAvailable(Long warehouseId, Long skuId) {
        if (warehouseId == null || skuId == null) return 0;
        WarehouseStock ws = warehouseStockMapper.selectOne(new LambdaQueryWrapper<WarehouseStock>()
                .eq(WarehouseStock::getWarehouseId, warehouseId)
                .eq(WarehouseStock::getSkuId, skuId));
        return ws != null && ws.getAvailable() != null ? ws.getAvailable() : 0;
    }

    @Override
    public Map<Long, Integer> getAvailableBatch(Long warehouseId, Collection<Long> skuIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (warehouseId == null || skuIds == null || skuIds.isEmpty()) return result;
        List<WarehouseStock> list = warehouseStockMapper.selectList(new LambdaQueryWrapper<WarehouseStock>()
                .eq(WarehouseStock::getWarehouseId, warehouseId)
                .in(WarehouseStock::getSkuId, skuIds));
        for (WarehouseStock ws : list) {
            result.put(ws.getSkuId(), ws.getAvailable() != null ? ws.getAvailable() : 0);
        }
        for (Long skuId : skuIds) {
            result.putIfAbsent(skuId, 0);
        }
        return result;
    }

    @Override
    @Transactional
    public void unlockForOrder(Order order) {
        List<StockLockDetail> details = stockLockDetailMapper.selectList(
                new LambdaQueryWrapper<StockLockDetail>()
                        .eq(StockLockDetail::getOrderId, order.getId())
                        .eq(StockLockDetail::getStatus, LOCK_STATUS_LOCKED));
        for (StockLockDetail d : details) {
            StockLot lot = stockLotMapper.selectById(d.getLotId());
            if (lot != null) {
                int before = lot.getAvailable();
                lot.setAvailable(lot.getAvailable() + d.getQty());
                lot.setLocked(Math.max(0, lot.getLocked() - d.getQty()));
                stockLotMapper.updateById(lot);
                writeLog(d.getWarehouseId(), d.getSkuId(), d.getLotNo(), "UNLOCK", d.getQty(),
                        before, lot.getAvailable(), "ORDER", order.getId(), null);
            }
            d.setStatus(LOCK_STATUS_RELEASED);
            stockLockDetailMapper.updateById(d);
            updateSummary(d.getWarehouseId(), d.getSkuId());
        }
    }

    @Override
    @Transactional
    public void consumeForOrder(Order order) {
        if (order == null || order.getId() == null) return;
        List<StockLockDetail> details = stockLockDetailMapper.selectList(
                new LambdaQueryWrapper<StockLockDetail>()
                        .eq(StockLockDetail::getOrderId, order.getId())
                        .eq(StockLockDetail::getStatus, LOCK_STATUS_LOCKED));
        if (details.isEmpty()) {
            log.warn("consumeForOrder: no locked details for orderId={}", order.getId());
            return;
        }
        for (StockLockDetail d : details) {
            StockLot lot = stockLotMapper.selectById(d.getLotId());
            if (lot == null) {
                log.warn("consumeForOrder: lot missing lotId={} orderId={}", d.getLotId(), order.getId());
                d.setStatus(LOCK_STATUS_CONSUMED);
                stockLockDetailMapper.updateById(d);
                continue;
            }
            int beforeLocked = lot.getLocked() != null ? lot.getLocked() : 0;
            int qty = d.getQty() != null ? d.getQty() : 0;
            if (beforeLocked < qty) {
                throw new BizException("锁定库存不足，无法扣减: lotNo=" + lot.getLotNo());
            }
            lot.setLocked(beforeLocked - qty);
            stockLotMapper.updateById(lot);
            // before/after 记录 locked 口径，便于对账
            writeLog(d.getWarehouseId(), d.getSkuId(), d.getLotNo(), "CONSUME", -qty,
                    beforeLocked, lot.getLocked(), "ORDER", order.getId(), null);
            d.setStatus(LOCK_STATUS_CONSUMED);
            stockLockDetailMapper.updateById(d);
            updateSummary(d.getWarehouseId(), d.getSkuId());
        }
    }

    /**
     * Post a confirmed stock order (IN increases available, OUT decreases available).
     */
    @Transactional
    public void postStockOrder(StockOrder stockOrder, List<StockOrderItem> items, List<StockOrderLot> lots) {
        for (StockOrderItem soItem : items) {
            List<StockOrderLot> itemLots = lots.stream()
                    .filter(l -> l.getStockOrderItemId().equals(soItem.getId()))
                    .toList();

            if (itemLots.isEmpty()) {
                // single default lot
                StockLot lot = ensureDefaultLot(stockOrder.getWarehouseId(), soItem.getSkuId());
                applyLotChange(stockOrder, lot, soItem.getQty());
            } else {
                for (StockOrderLot sol : itemLots) {
                    StockLot lot = sol.getLotId() != null ? stockLotMapper.selectById(sol.getLotId())
                            : findOrCreateLot(stockOrder.getWarehouseId(), soItem.getSkuId(), sol.getLotNo(), null);
                    applyLotChange(stockOrder, lot, sol.getQty());
                }
            }
            updateSummary(stockOrder.getWarehouseId(), soItem.getSkuId());
        }
    }

    private void applyLotChange(StockOrder so, StockLot lot, int qty) {
        int before = lot.getAvailable();
        if ("IN".equals(so.getOrderType())) {
            lot.setAvailable(lot.getAvailable() + qty);
        } else {
            if (lot.getAvailable() < qty) {
                throw new BizException("批次库存不足: lotNo=" + lot.getLotNo());
            }
            lot.setAvailable(lot.getAvailable() - qty);
        }
        stockLotMapper.updateById(lot);
        writeLog(so.getWarehouseId(), lot.getSkuId(), lot.getLotNo(),
                "IN".equals(so.getOrderType()) ? "INBOUND" : "OUTBOUND",
                "IN".equals(so.getOrderType()) ? qty : -qty,
                before, lot.getAvailable(), "STOCK_ORDER", so.getId(), null);
    }

    // --- lot helpers ---

    private List<StockLot> selectAllocatableLots(Long warehouseId, Long skuId) {
        // FEFO: order by expire_date ASC (nulls last), then inbound_at ASC (FIFO)
        List<StockLot> lots = stockLotMapper.selectList(new LambdaQueryWrapper<StockLot>()
                .eq(StockLot::getWarehouseId, warehouseId)
                .eq(StockLot::getSkuId, skuId)
                .gt(StockLot::getAvailable, 0));
        lots.sort(InventoryService::compareFefo);
        return lots;
    }

    /**
     * FEFO 比较：更早过期优先；过期日相同则更早入库优先；null 视为最晚。
     * package-visible 便于单测。
     */
    static int compareFefo(StockLot a, StockLot b) {
        long expA = a.getExpireDate() != null ? a.getExpireDate() : Long.MAX_VALUE;
        long expB = b.getExpireDate() != null ? b.getExpireDate() : Long.MAX_VALUE;
        int byExpire = Long.compare(expA, expB);
        if (byExpire != 0) {
            return byExpire;
        }
        long inA = a.getInboundAt() != null ? a.getInboundAt() : Long.MAX_VALUE;
        long inB = b.getInboundAt() != null ? b.getInboundAt() : Long.MAX_VALUE;
        return Long.compare(inA, inB);
    }

    StockLot ensureDefaultLot(Long warehouseId, Long skuId) {
        return findOrCreateLot(warehouseId, skuId, DEFAULT_LOT, null);
    }

    private StockLot findOrCreateLot(Long warehouseId, Long skuId, String lotNo, Long supplierId) {
        StockLot lot = stockLotMapper.selectOne(new LambdaQueryWrapper<StockLot>()
                .eq(StockLot::getWarehouseId, warehouseId)
                .eq(StockLot::getSkuId, skuId)
                .eq(StockLot::getLotNo, lotNo));
        if (lot != null) return lot;

        lot = new StockLot();
        lot.setWarehouseId(warehouseId);
        lot.setSkuId(skuId);
        lot.setLotNo(lotNo);
        lot.setAvailable(0);
        lot.setLocked(0);
        lot.setInTransit(0);
        lot.setVersion(0);
        lot.setInboundAt(System.currentTimeMillis());
        lot.setSupplierId(supplierId);
        stockLotMapper.insert(lot);
        return lot;
    }

    private void updateSummary(Long warehouseId, Long skuId) {
        WarehouseStock ws = warehouseStockMapper.selectOne(new LambdaQueryWrapper<WarehouseStock>()
                .eq(WarehouseStock::getWarehouseId, warehouseId)
                .eq(WarehouseStock::getSkuId, skuId));

        List<StockLot> allLots = stockLotMapper.selectList(new LambdaQueryWrapper<StockLot>()
                .eq(StockLot::getWarehouseId, warehouseId)
                .eq(StockLot::getSkuId, skuId));

        int totalAvail = allLots.stream().mapToInt(StockLot::getAvailable).sum();
        int totalLocked = allLots.stream().mapToInt(StockLot::getLocked).sum();
        int totalTransit = allLots.stream().mapToInt(StockLot::getInTransit).sum();

        if (ws == null) {
            ws = new WarehouseStock();
            ws.setWarehouseId(warehouseId);
            ws.setSkuId(skuId);
            ws.setAvailable(totalAvail);
            ws.setLocked(totalLocked);
            ws.setInTransit(totalTransit);
            ws.setVersion(0);
            warehouseStockMapper.insert(ws);
        } else {
            ws.setAvailable(totalAvail);
            ws.setLocked(totalLocked);
            ws.setInTransit(totalTransit);
            warehouseStockMapper.updateById(ws);
        }
    }

    private void writeLog(Long warehouseId, Long skuId, String lotNo, String changeType,
                          int changeQty, int before, int after, String refType, Long refId, String remark) {
        InventoryLog log = new InventoryLog();
        log.setWarehouseId(warehouseId);
        log.setSkuId(skuId);
        log.setLotNo(lotNo);
        log.setChangeType(changeType);
        log.setChangeQty(changeQty);
        log.setBeforeQty(before);
        log.setAfterQty(after);
        log.setRefType(refType);
        log.setRefId(refId);
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
    }
}
