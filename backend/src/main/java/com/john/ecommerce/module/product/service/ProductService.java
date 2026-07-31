package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.SpuCreateDTO;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final SpuMapper spuMapper;

    public SpuVO create(SpuCreateDTO dto) {
        Spu spu = new Spu();
        spu.setName(dto.getName());
        spu.setSubtitle(dto.getSubtitle());
        spu.setCategoryId(dto.getCategoryId());
        spu.setMerchantId(dto.getMerchantId());
        spu.setShopId(dto.getShopId());
        spu.setBrandId(dto.getBrandId());
        spu.setProductCode(dto.getProductCode());
        spu.setMainImages(dto.getMainImages());
        spu.setDetail(dto.getDetail());
        spu.setProductType(dto.getProductType() != null ? dto.getProductType() : 0);
        spu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        spu.setStatus(0);
        spu.setSales(0);
        spuMapper.insert(spu);
        return toVO(spu);
    }

    public SpuVO getById(Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) throw new BizException("商品不存在");
        return toVO(spu);
    }

    public Page<SpuVO> list(int page, int size, Integer status) {
        return list(page, size, status, null, null);
    }

    public Page<SpuVO> list(int page, int size, Integer status, Long shopId, Long merchantId) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<Spu>()
                .eq(status != null, Spu::getStatus, status)
                .eq(shopId != null, Spu::getShopId, shopId)
                .eq(merchantId != null, Spu::getMerchantId, merchantId)
                .orderByDesc(Spu::getCreatedAt);
        Page<Spu> p = spuMapper.selectPage(new Page<>(page, size), wrapper);
        Page<SpuVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public void updateStatus(Long id, Integer status) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) throw new BizException("商品不存在");
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    private SpuVO toVO(Spu s) {
        SpuVO vo = new SpuVO();
        vo.setId(s.getId());
        vo.setMerchantId(s.getMerchantId());
        vo.setShopId(s.getShopId());
        vo.setCategoryId(s.getCategoryId());
        vo.setBrandId(s.getBrandId());
        vo.setProductCode(s.getProductCode());
        vo.setName(s.getName());
        vo.setSubtitle(s.getSubtitle());
        vo.setMainImages(s.getMainImages());
        vo.setDetail(s.getDetail());
        vo.setProductType(s.getProductType());
        vo.setStatus(s.getStatus());
        vo.setSales(s.getSales());
        vo.setSortOrder(s.getSortOrder());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }
}
