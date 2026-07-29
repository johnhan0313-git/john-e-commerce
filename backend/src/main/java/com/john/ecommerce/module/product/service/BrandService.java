package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.BrandCreateDTO;
import com.john.ecommerce.module.product.dto.BrandVO;
import com.john.ecommerce.module.product.entity.Brand;
import com.john.ecommerce.module.product.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandMapper brandMapper;

    public BrandVO create(BrandCreateDTO dto) {
        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setLogo(dto.getLogo());
        brand.setDescription(dto.getDescription());
        brand.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        brand.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        brandMapper.insert(brand);
        return toVO(brand);
    }

    public BrandVO update(Long id, BrandCreateDTO dto) {
        Brand brand = require(id);
        brand.setName(dto.getName());
        brand.setLogo(dto.getLogo());
        brand.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) brand.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) brand.setStatus(dto.getStatus());
        brandMapper.updateById(brand);
        return toVO(brand);
    }

    public BrandVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<BrandVO> list(int page, int size, Integer status) {
        Page<Brand> p = brandMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Brand>()
                        .eq(status != null, Brand::getStatus, status)
                        .orderByAsc(Brand::getSortOrder)
                        .orderByDesc(Brand::getCreatedAt));
        Page<BrandVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public void delete(Long id) {
        require(id);
        brandMapper.deleteById(id);
    }

    private Brand require(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) throw new BizException("品牌不存在");
        return brand;
    }

    private BrandVO toVO(Brand b) {
        BrandVO vo = new BrandVO();
        vo.setId(b.getId());
        vo.setName(b.getName());
        vo.setLogo(b.getLogo());
        vo.setDescription(b.getDescription());
        vo.setSortOrder(b.getSortOrder());
        vo.setStatus(b.getStatus());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }
}
