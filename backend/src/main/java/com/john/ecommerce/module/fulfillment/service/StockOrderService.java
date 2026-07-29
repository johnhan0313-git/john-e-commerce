package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.StockOrderCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.StockOrderVO;
import com.john.ecommerce.module.fulfillment.entity.StockOrder;
import com.john.ecommerce.module.fulfillment.entity.StockOrderItem;
import com.john.ecommerce.module.fulfillment.entity.StockOrderLot;
import com.john.ecommerce.module.fulfillment.mapper.StockOrderItemMapper;
import com.john.ecommerce.module.fulfillment.mapper.StockOrderLotMapper;
import com.john.ecommerce.module.fulfillment.mapper.StockOrderMapper;
import com.john.ecommerce.module.fulfillment.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOrderService {

    private final StockOrderMapper stockOrderMapper;
    private final StockOrderItemMapper stockOrderItemMapper;
    private final StockOrderLotMapper stockOrderLotMapper;
    private final InventoryService inventoryService;

    @Transactional
    public StockOrderVO create(StockOrderCreateDTO dto) {
        StockOrder so = new StockOrder();
        so.setStockOrderNo("SO" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000)));
        so.setOrderType(dto.getOrderType());
        so.setBizType(dto.getBizType());
        so.setWarehouseId(dto.getWarehouseId());
        so.setRefNo(dto.getRefNo());
        so.setStatus(0);
        so.setRemark(dto.getRemark());
        stockOrderMapper.insert(so);

        List<StockOrderItem> items = new ArrayList<>();
        List<StockOrderLot> lots = new ArrayList<>();
        for (StockOrderCreateDTO.Item item : dto.getItems()) {
            StockOrderItem soi = new StockOrderItem();
            soi.setStockOrderId(so.getId());
            soi.setSkuId(item.getSkuId());
            soi.setQty(item.getQty());
            stockOrderItemMapper.insert(soi);
            items.add(soi);

            if (item.getLotNo() != null) {
                StockOrderLot sol = new StockOrderLot();
                sol.setStockOrderItemId(soi.getId());
                sol.setLotNo(item.getLotNo());
                sol.setQty(item.getQty());
                stockOrderLotMapper.insert(sol);
                lots.add(sol);
            }
        }
        return toVO(so, items, lots);
    }

    @Transactional
    public StockOrderVO confirm(Long id) {
        StockOrder so = require(id);
        if (so.getStatus() != 0) throw new BizException("出入库单当前状态不可确认");
        so.setStatus(1);
        so.setConfirmedAt(System.currentTimeMillis());
        stockOrderMapper.updateById(so);

        List<StockOrderItem> items = getItems(id);
        List<StockOrderLot> lots = getLots(items);
        inventoryService.postStockOrder(so, items, lots);
        return toVO(so, items, lots);
    }

    public StockOrderVO getById(Long id) {
        StockOrder so = require(id);
        List<StockOrderItem> items = getItems(id);
        return toVO(so, items, getLots(items));
    }

    public Page<StockOrderVO> list(int page, int size, String orderType) {
        Page<StockOrder> p = stockOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<StockOrder>()
                        .eq(orderType != null, StockOrder::getOrderType, orderType)
                        .orderByDesc(StockOrder::getCreatedAt));
        Page<StockOrderVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(so -> {
            List<StockOrderItem> items = getItems(so.getId());
            return toVO(so, items, getLots(items));
        }).toList());
        return result;
    }

    private StockOrder require(Long id) {
        StockOrder so = stockOrderMapper.selectById(id);
        if (so == null) throw new BizException("出入库单不存在");
        return so;
    }

    private List<StockOrderItem> getItems(Long soId) {
        return stockOrderItemMapper.selectList(new LambdaQueryWrapper<StockOrderItem>()
                .eq(StockOrderItem::getStockOrderId, soId));
    }

    private List<StockOrderLot> getLots(List<StockOrderItem> items) {
        if (items.isEmpty()) return List.of();
        List<Long> itemIds = items.stream().map(StockOrderItem::getId).toList();
        return stockOrderLotMapper.selectList(new LambdaQueryWrapper<StockOrderLot>()
                .in(StockOrderLot::getStockOrderItemId, itemIds));
    }

    private StockOrderVO toVO(StockOrder so, List<StockOrderItem> items, List<StockOrderLot> lots) {
        StockOrderVO vo = new StockOrderVO();
        vo.setId(so.getId());
        vo.setStockOrderNo(so.getStockOrderNo());
        vo.setOrderType(so.getOrderType());
        vo.setBizType(so.getBizType());
        vo.setWarehouseId(so.getWarehouseId());
        vo.setRefNo(so.getRefNo());
        vo.setStatus(so.getStatus());
        vo.setRemark(so.getRemark());
        vo.setConfirmedAt(so.getConfirmedAt());
        vo.setCreatedAt(so.getCreatedAt());
        vo.setItems(items.stream().map(i -> {
            StockOrderVO.ItemVO iv = new StockOrderVO.ItemVO();
            iv.setId(i.getId());
            iv.setSkuId(i.getSkuId());
            iv.setQty(i.getQty());
            iv.setActualQty(i.getActualQty());
            iv.setLots(lots.stream().filter(l -> l.getStockOrderItemId().equals(i.getId()))
                    .map(l -> {
                        StockOrderVO.LotVO lv = new StockOrderVO.LotVO();
                        lv.setId(l.getId());
                        lv.setLotId(l.getLotId());
                        lv.setLotNo(l.getLotNo());
                        lv.setQty(l.getQty());
                        return lv;
                    }).toList());
            return iv;
        }).toList());
        return vo;
    }
}
