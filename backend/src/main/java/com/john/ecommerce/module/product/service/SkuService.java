package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    public SkuVO create(SkuCreateDTO dto) {
        requireSpu(dto.getSpuId());
        Sku sku = new Sku();
        sku.setSpuId(dto.getSpuId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setSkuName(dto.getSkuName());
        sku.setSpecValues(dto.getSpecValues());
        sku.setPrice(dto.getPrice());
        sku.setCostPrice(dto.getCostPrice());
        sku.setLotEnabled(dto.getLotEnabled() != null ? dto.getLotEnabled() : false);
        sku.setWeight(dto.getWeight());
        sku.setBarcode(dto.getBarcode());
        sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        skuMapper.insert(sku);
        return toVO(sku);
    }

    public SkuVO update(Long id, SkuCreateDTO dto) {
        Sku sku = require(id);
        if (dto.getSkuName() != null) sku.setSkuName(dto.getSkuName());
        if (dto.getSkuCode() != null) sku.setSkuCode(dto.getSkuCode());
        if (dto.getSpecValues() != null) sku.setSpecValues(dto.getSpecValues());
        if (dto.getPrice() != null) sku.setPrice(dto.getPrice());
        if (dto.getCostPrice() != null) sku.setCostPrice(dto.getCostPrice());
        if (dto.getLotEnabled() != null) sku.setLotEnabled(dto.getLotEnabled());
        if (dto.getWeight() != null) sku.setWeight(dto.getWeight());
        if (dto.getBarcode() != null) sku.setBarcode(dto.getBarcode());
        if (dto.getStatus() != null) sku.setStatus(dto.getStatus());
        skuMapper.updateById(sku);
        return toVO(sku);
    }

    public SkuVO getById(Long id) {
        return toVO(require(id));
    }

    public List<SkuVO> listBySpu(Long spuId) {
        return skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spuId)
                        .orderByAsc(Sku::getId))
                .stream().map(this::toVO).toList();
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
        SkuVO vo = new SkuVO();
        vo.setId(s.getId());
        vo.setSpuId(s.getSpuId());
        vo.setSkuCode(s.getSkuCode());
        vo.setSkuName(s.getSkuName());
        vo.setSpecValues(s.getSpecValues());
        vo.setPrice(s.getPrice());
        vo.setCostPrice(s.getCostPrice());
        vo.setLotEnabled(s.getLotEnabled());
        vo.setWeight(s.getWeight());
        vo.setBarcode(s.getBarcode());
        vo.setStatus(s.getStatus());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }
}
