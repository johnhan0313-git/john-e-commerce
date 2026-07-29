package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.InventoryLogVO;
import com.john.ecommerce.module.fulfillment.entity.InventoryLog;
import com.john.ecommerce.module.fulfillment.mapper.InventoryLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/logs")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.FULFILLMENT)
public class InventoryLogController {

    private final InventoryLogMapper inventoryLogMapper;

    @GetMapping
    public R<Page<InventoryLogVO>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) Long warehouseId,
                                        @RequestParam(required = false) Long skuId) {
        LambdaQueryWrapper<InventoryLog> w = new LambdaQueryWrapper<InventoryLog>()
                .eq(warehouseId != null, InventoryLog::getWarehouseId, warehouseId)
                .eq(skuId != null, InventoryLog::getSkuId, skuId)
                .orderByDesc(InventoryLog::getCreatedAt);
        Page<InventoryLog> p = inventoryLogMapper.selectPage(new Page<>(page, size), w);
        Page<InventoryLogVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return R.ok(result);
    }

    private InventoryLogVO toVO(InventoryLog l) {
        InventoryLogVO vo = new InventoryLogVO();
        vo.setId(l.getId());
        vo.setWarehouseId(l.getWarehouseId());
        vo.setSkuId(l.getSkuId());
        vo.setLotNo(l.getLotNo());
        vo.setChangeType(l.getChangeType());
        vo.setChangeQty(l.getChangeQty());
        vo.setBeforeQty(l.getBeforeQty());
        vo.setAfterQty(l.getAfterQty());
        vo.setRefType(l.getRefType());
        vo.setRefId(l.getRefId());
        vo.setRemark(l.getRemark());
        vo.setCreatedAt(l.getCreatedAt());
        return vo;
    }
}
