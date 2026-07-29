package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.WarehouseCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.WarehouseStockVO;
import com.john.ecommerce.module.fulfillment.dto.WarehouseVO;
import com.john.ecommerce.module.fulfillment.entity.Warehouse;
import com.john.ecommerce.module.fulfillment.entity.WarehouseStock;
import com.john.ecommerce.module.fulfillment.mapper.WarehouseMapper;
import com.john.ecommerce.module.fulfillment.mapper.WarehouseStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final WarehouseStockMapper warehouseStockMapper;

    public WarehouseVO create(WarehouseCreateDTO dto) {
        Warehouse w = new Warehouse();
        w.setCode(dto.getCode());
        w.setName(dto.getName());
        w.setMerchantId(dto.getMerchantId());
        w.setAddress(dto.getAddress());
        w.setStatus(1);
        warehouseMapper.insert(w);
        return toVO(w);
    }

    public WarehouseVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<WarehouseVO> list(int page, int size) {
        Page<Warehouse> p = warehouseMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Warehouse>().orderByDesc(Warehouse::getCreatedAt));
        Page<WarehouseVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public List<WarehouseStockVO> stockByWarehouse(Long warehouseId) {
        return warehouseStockMapper.selectList(new LambdaQueryWrapper<WarehouseStock>()
                        .eq(WarehouseStock::getWarehouseId, warehouseId))
                .stream().map(this::toStockVO).toList();
    }

    public List<WarehouseStockVO> stockBySku(Long skuId) {
        return warehouseStockMapper.selectList(new LambdaQueryWrapper<WarehouseStock>()
                        .eq(WarehouseStock::getSkuId, skuId))
                .stream().map(this::toStockVO).toList();
    }

    private Warehouse require(Long id) {
        Warehouse w = warehouseMapper.selectById(id);
        if (w == null) throw new BizException("仓库不存在");
        return w;
    }

    private WarehouseVO toVO(Warehouse w) {
        WarehouseVO vo = new WarehouseVO();
        vo.setId(w.getId());
        vo.setMerchantId(w.getMerchantId());
        vo.setCode(w.getCode());
        vo.setName(w.getName());
        vo.setAddress(w.getAddress());
        vo.setStatus(w.getStatus());
        vo.setCreatedAt(w.getCreatedAt());
        return vo;
    }

    private WarehouseStockVO toStockVO(WarehouseStock ws) {
        WarehouseStockVO vo = new WarehouseStockVO();
        vo.setId(ws.getId());
        vo.setWarehouseId(ws.getWarehouseId());
        vo.setSkuId(ws.getSkuId());
        vo.setAvailable(ws.getAvailable());
        vo.setLocked(ws.getLocked());
        vo.setInTransit(ws.getInTransit());
        return vo;
    }
}
