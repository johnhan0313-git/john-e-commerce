package com.john.ecommerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.dto.AddressCreateDTO;
import com.john.ecommerce.module.user.dto.AddressVO;
import com.john.ecommerce.module.user.entity.UserAddress;
import com.john.ecommerce.module.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressMapper addressMapper;

    public AddressVO create(AddressCreateDTO dto) {
        Long userId = requireUserId();
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setName(dto.getName());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setPostalCode(dto.getPostalCode());
        boolean isDefault = Boolean.TRUE.equals(dto.getIsDefault());
        addr.setIsDefault(isDefault);
        if (isDefault) {
            clearDefault(userId);
        }
        addressMapper.insert(addr);
        return toVO(addr);
    }

    public AddressVO update(Long id, AddressCreateDTO dto) {
        UserAddress addr = requireOwned(id);
        addr.setName(dto.getName());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setPostalCode(dto.getPostalCode());
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefault(addr.getUserId());
            addr.setIsDefault(true);
        } else if (dto.getIsDefault() != null) {
            addr.setIsDefault(false);
        }
        addressMapper.updateById(addr);
        return toVO(addr);
    }

    public AddressVO getById(Long id) {
        return toVO(requireOwned(id));
    }

    public List<AddressVO> listMine() {
        Long userId = requireUserId();
        return addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getUpdatedAt))
                .stream().map(this::toVO).toList();
    }

    public void delete(Long id) {
        requireOwned(id);
        addressMapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Long id) {
        UserAddress addr = requireOwned(id);
        clearDefault(addr.getUserId());
        addr.setIsDefault(true);
        addressMapper.updateById(addr);
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, true)
                .set(UserAddress::getIsDefault, false));
    }

    private UserAddress requireOwned(Long id) {
        Long userId = requireUserId();
        UserAddress addr = addressMapper.selectById(id);
        if (addr == null || !userId.equals(addr.getUserId())) {
            throw new BizException("地址不存在");
        }
        return addr;
    }

    private Long requireUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }
        return userId;
    }

    private AddressVO toVO(UserAddress a) {
        AddressVO vo = new AddressVO();
        vo.setId(a.getId());
        vo.setUserId(a.getUserId());
        vo.setName(a.getName());
        vo.setPhone(a.getPhone());
        vo.setProvince(a.getProvince());
        vo.setCity(a.getCity());
        vo.setDistrict(a.getDistrict());
        vo.setDetail(a.getDetail());
        vo.setPostalCode(a.getPostalCode());
        vo.setIsDefault(a.getIsDefault());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
