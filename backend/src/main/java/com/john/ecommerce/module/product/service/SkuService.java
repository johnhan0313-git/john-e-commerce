package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.port.inventory.InventoryPort;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuService {

    /** 与 OrderSplitter 默认仓一致 */
    private static final long DEFAULT_WAREHOUSE_ID = 0L;

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final InventoryPort inventoryPort;

    public SkuVO create(SkuCreateDTO dto) {
        requireSpu(dto.getSpuId());
        Sku sku = new Sku();
        sku.setSpuId(dto.getSpuId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setSkuName(dto.getSkuName());
        sku.setSpecValues(dto.getSpecValues());
        sku.setPrice(dto.getPrice());
        sku.setCostPrice(dto.getCostPrice());
        sku.setLotEnabled(toLotFlag(dto.getLotEnabled()));
        sku.setWeight(dto.getWeight());
        sku.setBarcode(dto.getBarcode());
        sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        skuMapper.insert(sku);
        int initStock = dto.getInitStock() != null ? dto.getInitStock() : 0;
        inventoryPort.initOrSetAvailable(DEFAULT_WAREHOUSE_ID, sku.getId(), initStock);
        return toVO(sku);
    }

    public SkuVO update(Long id, SkuCreateDTO dto) {
        Sku sku = require(id);
        if (dto.getSkuName() != null) sku.setSkuName(dto.getSkuName());
        if (dto.getSkuCode() != null) sku.setSkuCode(dto.getSkuCode());
        if (dto.getSpecValues() != null) sku.setSpecValues(dto.getSpecValues());
        if (dto.getPrice() != null) sku.setPrice(dto.getPrice());
        if (dto.getCostPrice() != null) sku.setCostPrice(dto.getCostPrice());
        if (dto.getLotEnabled() != null) sku.setLotEnabled(toLotFlag(dto.getLotEnabled()));
        if (dto.getWeight() != null) sku.setWeight(dto.getWeight());
        if (dto.getBarcode() != null) sku.setBarcode(dto.getBarcode());
        if (dto.getStatus() != null) sku.setStatus(dto.getStatus());
        skuMapper.updateById(sku);
        if (dto.getInitStock() != null) {
            inventoryPort.initOrSetAvailable(DEFAULT_WAREHOUSE_ID, sku.getId(), dto.getInitStock());
        }
        return toVO(sku);
    }

    public SkuVO getById(Long id) {
        return toVO(require(id));
    }

    public List<SkuVO> listBySpu(Long spuId) {
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .orderByAsc(Sku::getId));
        Map<Long, Integer> avail = inventoryPort.getAvailableBatch(
                DEFAULT_WAREHOUSE_ID,
                skus.stream().map(Sku::getId).collect(Collectors.toList()));
        return skus.stream().map(s -> toVO(s, avail.getOrDefault(s.getId(), 0))).toList();
    }

    public void delete(Long id) {
        require(id);
        skuMapper.deleteById(id);
    }

    private Spu requireSpu(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BizException("商品不存在");
        return spu;
    }

    private Sku require(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) throw new BizException("SKU不存在");
        return sku;
    }

    private SkuVO toVO(Sku s) {
        return toVO(s, inventoryPort.getAvailable(DEFAULT_WAREHOUSE_ID, s.getId()));
    }

    private SkuVO toVO(Sku s, int available) {
        SkuVO vo = new SkuVO();
        vo.setId(s.getId());
        vo.setSpuId(s.getSpuId());
        vo.setSkuCode(s.getSkuCode());
        vo.setSkuName(s.getSkuName());
        vo.setSpecValues(s.getSpecValues());
        vo.setPrice(s.getPrice());
        vo.setCostPrice(s.getCostPrice());
        vo.setLotEnabled(s.getLotEnabled() != null && s.getLotEnabled() == 1);
        vo.setWeight(s.getWeight());
        vo.setBarcode(s.getBarcode());
        vo.setStatus(s.getStatus());
        vo.setAvailable(available);
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    private static int toLotFlag(Boolean lotEnabled) {
        return Boolean.TRUE.equals(lotEnabled) ? 1 : 0;
    }
}
