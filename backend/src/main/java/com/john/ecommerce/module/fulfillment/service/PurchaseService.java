package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.PurchaseOrderCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.PurchaseOrderVO;
import com.john.ecommerce.module.fulfillment.entity.*;
import com.john.ecommerce.module.fulfillment.mapper.*;
import com.john.ecommerce.module.fulfillment.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final StockOrderMapper stockOrderMapper;
    private final StockOrderItemMapper stockOrderItemMapper;
    private final StockOrderLotMapper stockOrderLotMapper;
    private final InventoryService inventoryService;

    @Transactional
    public PurchaseOrderVO create(PurchaseOrderCreateDTO dto) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNo(generatePoNo());
        po.setSupplierId(dto.getSupplierId());
        po.setWarehouseId(dto.getWarehouseId());
        po.setRefActivityId(dto.getRefActivityId());
        po.setStatus("DRAFT");
        po.setRemark(dto.getRemark());
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderCreateDTO.Item item : dto.getItems()) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
        }
        po.setTotalAmount(total);
        purchaseOrderMapper.insert(po);

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderCreateDTO.Item item : dto.getItems()) {
            PurchaseOrderItem pi = new PurchaseOrderItem();
            pi.setPurchaseOrderId(po.getId());
            pi.setSkuId(item.getSkuId());
            pi.setQty(item.getQty());
            pi.setReceivedQty(0);
            pi.setPrice(item.getPrice());
            purchaseOrderItemMapper.insert(pi);
            items.add(pi);
        }
        return toVO(po, items);
    }

    @Transactional
    public PurchaseOrderVO approve(Long id) {
        PurchaseOrder po = require(id);
        if (!"DRAFT".equals(po.getStatus())) throw new BizException("采购单当前状态不可审批");
        po.setStatus("APPROVED");
        po.setApprovedAt(System.currentTimeMillis());
        purchaseOrderMapper.updateById(po);
        return toVO(po, getItems(id));
    }

    @Transactional
    public PurchaseOrderVO receive(Long id) {
        PurchaseOrder po = require(id);
        if (!"APPROVED".equals(po.getStatus())) throw new BizException("采购单当前状态不可收货");

        List<PurchaseOrderItem> items = getItems(id);

        // create IN stock order
        StockOrder so = new StockOrder();
        so.setStockOrderNo("SO" + System.currentTimeMillis());
        so.setOrderType("IN");
        so.setBizType("PURCHASE");
        so.setWarehouseId(po.getWarehouseId());
        so.setRefNo(po.getPoNo());
        so.setStatus(1);
        so.setConfirmedAt(System.currentTimeMillis());
        stockOrderMapper.insert(so);

        List<StockOrderItem> soItems = new ArrayList<>();
        List<StockOrderLot> soLots = new ArrayList<>();
        for (PurchaseOrderItem pi : items) {
            int receiveQty = pi.getQty() - pi.getReceivedQty();
            if (receiveQty <= 0) continue;

            StockOrderItem soi = new StockOrderItem();
            soi.setStockOrderId(so.getId());
            soi.setSkuId(pi.getSkuId());
            soi.setQty(receiveQty);
            soi.setActualQty(receiveQty);
            stockOrderItemMapper.insert(soi);
            soItems.add(soi);

            pi.setReceivedQty(pi.getQty());
            purchaseOrderItemMapper.updateById(pi);
        }

        inventoryService.postStockOrder(so, soItems, soLots);

        po.setStatus("RECEIVED");
        po.setFinishedAt(System.currentTimeMillis());
        purchaseOrderMapper.updateById(po);
        return toVO(po, items);
    }

    public PurchaseOrderVO getById(Long id) {
        return toVO(require(id), getItems(id));
    }

    public Page<PurchaseOrderVO> list(int page, int size, String status) {
        Page<PurchaseOrder> p = purchaseOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(status != null, PurchaseOrder::getStatus, status)
                        .orderByDesc(PurchaseOrder::getCreatedAt));
        Page<PurchaseOrderVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(po -> toVO(po, getItems(po.getId()))).toList());
        return result;
    }

    private PurchaseOrder require(Long id) {
        PurchaseOrder po = purchaseOrderMapper.selectById(id);
        if (po == null) throw new BizException("采购单不存在");
        return po;
    }

    private List<PurchaseOrderItem> getItems(Long poId) {
        return purchaseOrderItemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getPurchaseOrderId, poId));
    }

    private PurchaseOrderVO toVO(PurchaseOrder po, List<PurchaseOrderItem> items) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(po.getId());
        vo.setPoNo(po.getPoNo());
        vo.setSupplierId(po.getSupplierId());
        vo.setWarehouseId(po.getWarehouseId());
        vo.setRefActivityId(po.getRefActivityId());
        vo.setStatus(po.getStatus());
        vo.setTotalAmount(po.getTotalAmount());
        vo.setRemark(po.getRemark());
        vo.setApprovedAt(po.getApprovedAt());
        vo.setFinishedAt(po.getFinishedAt());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setItems(items.stream().map(i -> {
            PurchaseOrderVO.ItemVO iv = new PurchaseOrderVO.ItemVO();
            iv.setId(i.getId());
            iv.setSkuId(i.getSkuId());
            iv.setQty(i.getQty());
            iv.setReceivedQty(i.getReceivedQty());
            iv.setPrice(i.getPrice());
            return iv;
        }).toList());
        return vo;
    }

    private String generatePoNo() {
        return "PO" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
