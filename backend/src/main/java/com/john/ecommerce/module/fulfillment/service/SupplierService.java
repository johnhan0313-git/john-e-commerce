package com.john.ecommerce.module.fulfillment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.SupplierCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.SupplierVO;
import com.john.ecommerce.module.fulfillment.entity.Supplier;
import com.john.ecommerce.module.fulfillment.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierVO create(SupplierCreateDTO dto) {
        Supplier s = new Supplier();
        s.setName(dto.getName());
        s.setContactName(dto.getContactName());
        s.setContactPhone(dto.getContactPhone());
        s.setAddress(dto.getAddress());
        s.setStatus(1);
        supplierMapper.insert(s);
        return toVO(s);
    }

    public SupplierVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<SupplierVO> list(int page, int size) {
        Page<Supplier> p = supplierMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Supplier>().orderByDesc(Supplier::getCreatedAt));
        Page<SupplierVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private Supplier require(Long id) {
        Supplier s = supplierMapper.selectById(id);
        if (s == null) throw new BizException("供应商不存在");
        return s;
    }

    private SupplierVO toVO(Supplier s) {
        SupplierVO vo = new SupplierVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setContactName(s.getContactName());
        vo.setContactPhone(s.getContactPhone());
        vo.setAddress(s.getAddress());
        vo.setStatus(s.getStatus());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }
}
