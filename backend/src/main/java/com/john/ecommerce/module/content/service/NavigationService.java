package com.john.ecommerce.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.content.dto.NavigationVO;
import com.john.ecommerce.module.content.entity.Navigation;
import com.john.ecommerce.module.content.mapper.NavigationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final NavigationMapper navigationMapper;

    public NavigationVO create(Navigation entity) {
        navigationMapper.insert(entity);
        return toVO(entity);
    }

    public NavigationVO update(Long id, Navigation entity) {
        Navigation existing = require(id);
        existing.setName(entity.getName());
        existing.setIconUrl(entity.getIconUrl());
        existing.setLinkUrl(entity.getLinkUrl());
        if (entity.getSortOrder() != null) existing.setSortOrder(entity.getSortOrder());
        existing.setParentId(entity.getParentId());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        navigationMapper.updateById(existing);
        return toVO(existing);
    }

    public NavigationVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<NavigationVO> list(int page, int size) {
        Page<Navigation> p = navigationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Navigation>().orderByAsc(Navigation::getSortOrder));
        Page<NavigationVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public List<NavigationVO> tree() {
        List<Navigation> all = navigationMapper.selectList(
                new LambdaQueryWrapper<Navigation>()
                        .eq(Navigation::getStatus, 1)
                        .orderByAsc(Navigation::getSortOrder));
        List<NavigationVO> vos = all.stream().map(this::toVO).toList();
        Map<Long, List<NavigationVO>> byParent = vos.stream()
                .filter(v -> v.getParentId() != null && v.getParentId() != 0)
                .collect(Collectors.groupingBy(NavigationVO::getParentId));
        List<NavigationVO> roots = new ArrayList<>();
        for (NavigationVO vo : vos) {
            vo.setChildren(byParent.getOrDefault(vo.getId(), List.of()));
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            }
        }
        return roots;
    }

    public void delete(Long id) {
        require(id);
        navigationMapper.deleteById(id);
    }

    private Navigation require(Long id) {
        Navigation n = navigationMapper.selectById(id);
        if (n == null) throw new BizException("导航不存在");
        return n;
    }

    private NavigationVO toVO(Navigation n) {
        NavigationVO vo = new NavigationVO();
        vo.setId(n.getId());
        vo.setName(n.getName());
        vo.setIconUrl(n.getIconUrl());
        vo.setLinkUrl(n.getLinkUrl());
        vo.setSortOrder(n.getSortOrder());
        vo.setParentId(n.getParentId());
        vo.setStatus(n.getStatus());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
