package com.john.ecommerce.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.content.dto.BannerVO;
import com.john.ecommerce.module.content.entity.Banner;
import com.john.ecommerce.module.content.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerMapper bannerMapper;

    public BannerVO create(Banner entity) {
        bannerMapper.insert(entity);
        return toVO(entity);
    }

    public BannerVO update(Long id, Banner entity) {
        Banner existing = require(id);
        existing.setTitle(entity.getTitle());
        existing.setImageUrl(entity.getImageUrl());
        existing.setLinkUrl(entity.getLinkUrl());
        existing.setPosition(entity.getPosition());
        if (entity.getSortOrder() != null) existing.setSortOrder(entity.getSortOrder());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        existing.setStartTime(entity.getStartTime());
        existing.setEndTime(entity.getEndTime());
        bannerMapper.updateById(existing);
        return toVO(existing);
    }

    public BannerVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<BannerVO> list(int page, int size) {
        Page<Banner> p = bannerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder));
        Page<BannerVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public List<BannerVO> listActive() {
        List<Banner> banners = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSortOrder));
        return banners.stream().map(this::toVO).toList();
    }

    public void delete(Long id) {
        require(id);
        bannerMapper.deleteById(id);
    }

    private Banner require(Long id) {
        Banner b = bannerMapper.selectById(id);
        if (b == null) throw new BizException("Banner不存在");
        return b;
    }

    private BannerVO toVO(Banner b) {
        BannerVO vo = new BannerVO();
        vo.setId(b.getId());
        vo.setTitle(b.getTitle());
        vo.setImageUrl(b.getImageUrl());
        vo.setLinkUrl(b.getLinkUrl());
        vo.setPosition(b.getPosition());
        vo.setSortOrder(b.getSortOrder());
        vo.setStatus(b.getStatus());
        vo.setStartTime(b.getStartTime());
        vo.setEndTime(b.getEndTime());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }
}
