package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.StockTransferCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.StockTransferVO;
import com.john.ecommerce.module.fulfillment.entity.*;
import com.john.ecommerce.module.fulfillment.mapper.*;
import com.john.ecommerce.module.fulfillment.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferMapper stockTransferMapper;
    private final StockTransferItemMapper stockTransferItemMapper;
    private final StockOrderMapper stockOrderMapper;
    private final StockOrderItemMapper stockOrderItemMapper;
    private final InventoryService inventoryService;

    @Transactional
    public StockTransferVO create(StockTransferCreateDTO dto) {
        if (dto.getFromWarehouseId().equals(dto.getToWarehouseId())) {
            throw new BizException("源仓和目标仓不能相同");
        }
        StockTransfer t = new StockTransfer();
        t.setTransferNo("TR" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000)));
        t.setFromWarehouseId(dto.getFromWarehouseId());
        t.setToWarehouseId(dto.getToWarehouseId());
        t.setStatus(0);
        t.setRemark(dto.getRemark());
        stockTransferMapper.insert(t);

        List<StockTransferItem> items = new ArrayList<>();
        for (StockTransferCreateDTO.Item item : dto.getItems()) {
            StockTransferItem ti = new StockTransferItem();
            ti.setTransferId(t.getId());
            ti.setSkuId(item.getSkuId());
            ti.setQty(item.getQty());
            stockTransferItemMapper.insert(ti);
            items.add(ti);
        }
        return toVO(t, items);
    }

    @Transactional
    public StockTransferVO confirm(Long id) {
        StockTransfer t = require(id);
        if (t.getStatus() != 0) throw new BizException("调拨单当前状态不可确认");
        List<StockTransferItem> items = getItems(id);

        // OUT from source warehouse
        StockOrder outOrder = createStockOrder("OUT", "TRANSFER", t.getFromWarehouseId(), t.getTransferNo());
        List<StockOrderItem> outItems = createStockOrderItems(outOrder.getId(), items);
        inventoryService.postStockOrder(outOrder, outItems, List.of());

        // IN to destination warehouse
        StockOrder inOrder = createStockOrder("IN", "TRANSFER", t.getToWarehouseId(), t.getTransferNo());
        List<StockOrderItem> inItems = createStockOrderItems(inOrder.getId(), items);
        inventoryService.postStockOrder(inOrder, inItems, List.of());

        t.setStatus(2);
        t.setShippedAt(System.currentTimeMillis());
        t.setReceivedAt(System.currentTimeMillis());
        stockTransferMapper.updateById(t);
        return toVO(t, items);
    }

    public StockTransferVO getById(Long id) {
        return toVO(require(id), getItems(id));
    }

    private StockTransfer require(Long id) {
        StockTransfer t = stockTransferMapper.selectById(id);
        if (t == null) throw new BizException("调拨单不存在");
        return t;
    }

    private List<StockTransferItem> getItems(Long transferId) {
        return stockTransferItemMapper.selectList(new LambdaQueryWrapper<StockTransferItem>()
                .eq(StockTransferItem::getTransferId, transferId));
    }

    private StockOrder createStockOrder(String orderType, String bizType, Long warehouseId, String refNo) {
        StockOrder so = new StockOrder();
        so.setStockOrderNo("SO" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000)));
        so.setOrderType(orderType);
        so.setBizType(bizType);
        so.setWarehouseId(warehouseId);
        so.setRefNo(refNo);
        so.setStatus(1);
        so.setConfirmedAt(System.currentTimeMillis());
        stockOrderMapper.insert(so);
        return so;
    }

    private List<StockOrderItem> createStockOrderItems(Long stockOrderId, List<StockTransferItem> transferItems) {
        List<StockOrderItem> result = new ArrayList<>();
        for (StockTransferItem ti : transferItems) {
            StockOrderItem soi = new StockOrderItem();
            soi.setStockOrderId(stockOrderId);
            soi.setSkuId(ti.getSkuId());
            soi.setQty(ti.getQty());
            soi.setActualQty(ti.getQty());
            stockOrderItemMapper.insert(soi);
            result.add(soi);
        }
        return result;
    }

    private StockTransferVO toVO(StockTransfer t, List<StockTransferItem> items) {
        StockTransferVO vo = new StockTransferVO();
        vo.setId(t.getId());
        vo.setTransferNo(t.getTransferNo());
        vo.setFromWarehouseId(t.getFromWarehouseId());
        vo.setToWarehouseId(t.getToWarehouseId());
        vo.setStatus(t.getStatus());
        vo.setRemark(t.getRemark());
        vo.setShippedAt(t.getShippedAt());
        vo.setReceivedAt(t.getReceivedAt());
        vo.setCreatedAt(t.getCreatedAt());
        vo.setItems(items.stream().map(i -> {
            StockTransferVO.ItemVO iv = new StockTransferVO.ItemVO();
            iv.setId(i.getId());
            iv.setSkuId(i.getSkuId());
            iv.setQty(i.getQty());
            return iv;
        }).toList());
        return vo;
    }
}
