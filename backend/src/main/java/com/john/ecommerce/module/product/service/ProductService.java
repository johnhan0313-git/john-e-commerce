package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.SalesAttrDTO;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuItemDTO;
import com.john.ecommerce.module.product.dto.SpuCreateDTO;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final SpuMapper spuMapper;
    private final SkuService skuService;

    @Transactional
    public SpuVO create(SpuCreateDTO dto) {
        Spu spu = new Spu();
        spu.setName(dto.getName().trim());
        spu.setSubtitle(blankToNull(dto.getSubtitle()));
        spu.setCategoryId(dto.getCategoryId());
        spu.setMerchantId(dto.getMerchantId());
        spu.setShopId(dto.getShopId());
        spu.setBrandId(dto.getBrandId());
        spu.setProductCode(blankToNull(dto.getProductCode()));
        spu.setMainImages(dto.getMainImages());
        spu.setDetail(blankToNull(dto.getDetail()));
        spu.setProductType(dto.getProductType() != null ? dto.getProductType() : 0);
        spu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        spu.setSalesAttrs(normalizeSalesAttrs(dto.getSalesAttrs()));
        spu.setStatus(0);
        spu.setSales(0);
        spuMapper.insert(spu);

        if (dto.getSkus() != null) {
            for (SkuItemDTO item : dto.getSkus()) {
                skuService.create(toSkuCreate(spu.getId(), item));
            }
        }
        return toVO(spu);
    }

    @Transactional
    public SpuVO update(Long id, SpuCreateDTO dto) {
        Spu spu = require(id);
        spu.setName(dto.getName().trim());
        spu.setSubtitle(blankToNull(dto.getSubtitle()));
        if (dto.getCategoryId() != null) spu.setCategoryId(dto.getCategoryId());
        if (dto.getBrandId() != null) spu.setBrandId(dto.getBrandId());
        if (dto.getProductCode() != null) spu.setProductCode(blankToNull(dto.getProductCode()));
        spu.setMainImages(dto.getMainImages());
        spu.setDetail(blankToNull(dto.getDetail()));
        if (dto.getProductType() != null) spu.setProductType(dto.getProductType());
        if (dto.getSortOrder() != null) spu.setSortOrder(dto.getSortOrder());
        // 显式传 salesAttrs（含空）则覆盖；前端编辑页总会带
        if (dto.getSalesAttrs() != null) {
            spu.setSalesAttrs(normalizeSalesAttrs(dto.getSalesAttrs()));
        }
        // merchantId / shopId / status 不在此接口改
        spuMapper.updateById(spu);
        if (dto.getSkus() != null) {
            syncSkus(spu.getId(), dto.getSkus());
        }
        return toVO(spu);
    }

    /** 按提交列表同步 SKU：有 id 更新，无 id 新建，缺席的删除 */
    private void syncSkus(Long spuId, List<SkuItemDTO> items) {
        Set<Long> owned = skuService.listBySpu(spuId).stream()
                .map(com.john.ecommerce.module.product.dto.SkuVO::getId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<Long> keep = new HashSet<>();
        for (SkuItemDTO item : items) {
            if (item.getId() != null) {
                if (!owned.contains(item.getId())) {
                    throw new BizException("SKU 不属于该商品: " + item.getId());
                }
                skuService.update(item.getId(), toSkuCreate(spuId, item));
                keep.add(item.getId());
            } else {
                keep.add(skuService.create(toSkuCreate(spuId, item)).getId());
            }
        }
        for (Long oldId : owned) {
            if (!keep.contains(oldId)) {
                skuService.delete(oldId);
            }
        }
    }

    public SpuVO getById(Long id) {
        return toVO(require(id));
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
        Spu spu = require(id);
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    public Spu require(Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) throw new BizException("商品不存在");
        return spu;
    }

    private SkuCreateDTO toSkuCreate(Long spuId, SkuItemDTO item) {
        SkuCreateDTO dto = new SkuCreateDTO();
        dto.setSpuId(spuId);
        dto.setSkuCode(blankToNull(item.getSkuCode()));
        dto.setSkuName(blankToNull(item.getSkuName()));
        dto.setSpecValues(item.getSpecValues());
        dto.setPrice(item.getPrice());
        dto.setCostPrice(item.getCostPrice());
        dto.setInitStock(item.getInitStock());
        dto.setStatus(item.getStatus() != null ? item.getStatus() : 1);
        return dto;
    }

    private static List<SalesAttrDTO> normalizeSalesAttrs(List<SalesAttrDTO> attrs) {
        if (attrs == null || attrs.isEmpty()) return null;
        List<SalesAttrDTO> out = new ArrayList<>();
        for (SalesAttrDTO a : attrs) {
            if (a == null || a.getName() == null || a.getName().isBlank()) continue;
            List<String> values = a.getValues() == null ? List.of() : a.getValues().stream()
                    .filter(v -> v != null && !v.isBlank())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            if (values.isEmpty()) continue;
            SalesAttrDTO n = new SalesAttrDTO();
            n.setName(a.getName().trim());
            n.setValues(values);
            out.add(n);
        }
        return out.isEmpty() ? null : out;
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
        vo.setSalesAttrs(s.getSalesAttrs());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }
}
