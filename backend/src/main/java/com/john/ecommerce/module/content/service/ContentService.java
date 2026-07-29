package com.john.ecommerce.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.content.dto.*;
import com.john.ecommerce.module.content.entity.Banner;
import com.john.ecommerce.module.content.entity.Navigation;
import com.john.ecommerce.module.content.entity.PageTemplate;
import com.john.ecommerce.module.content.mapper.BannerMapper;
import com.john.ecommerce.module.content.mapper.NavigationMapper;
import com.john.ecommerce.module.content.mapper.PageTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final PageTemplateMapper pageTemplateMapper;
    private final BannerMapper bannerMapper;
    private final NavigationMapper navigationMapper;

    // ---- PageTemplate ----

    public PageTemplateVO createPage(PageTemplateCreateDTO dto) {
        PageTemplate entity = new PageTemplate();
        entity.setName(dto.getName());
        entity.setTemplateType(dto.getTemplateType());
        entity.setConfig(dto.getConfig() != null ? dto.getConfig() : Collections.emptyMap());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        pageTemplateMapper.insert(entity);
        return toPageVO(entity);
    }

    public PageTemplateVO updatePage(Long id, PageTemplateCreateDTO dto) {
        PageTemplate entity = requirePage(id);
        entity.setName(dto.getName());
        entity.setTemplateType(dto.getTemplateType());
        if (dto.getConfig() != null) entity.setConfig(dto.getConfig());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        pageTemplateMapper.updateById(entity);
        return toPageVO(entity);
    }

    public PageTemplateVO getPageById(Long id) {
        return toPageVO(requirePage(id));
    }

    public Page<PageTemplateVO> listPages(int page, int size) {
        Page<PageTemplate> p = pageTemplateMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PageTemplate>().orderByDesc(PageTemplate::getCreatedAt));
        Page<PageTemplateVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toPageVO).toList());
        return result;
    }

    public void deletePage(Long id) {
        pageTemplateMapper.deleteById(id);
    }

    // ---- Banner ----

    public BannerVO createBanner(BannerCreateDTO dto) {
        Banner entity = new Banner();
        entity.setTitle(dto.getTitle());
        entity.setImageUrl(dto.getImageUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setPosition(dto.getPosition() != null ? dto.getPosition() : "HOME");
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        bannerMapper.insert(entity);
        return toBannerVO(entity);
    }

    public BannerVO updateBanner(Long id, BannerCreateDTO dto) {
        Banner entity = requireBanner(id);
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getImageUrl() != null) entity.setImageUrl(dto.getImageUrl());
        if (dto.getLinkUrl() != null) entity.setLinkUrl(dto.getLinkUrl());
        if (dto.getPosition() != null) entity.setPosition(dto.getPosition());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        bannerMapper.updateById(entity);
        return toBannerVO(entity);
    }

    public BannerVO getBannerById(Long id) {
        return toBannerVO(requireBanner(id));
    }

    public Page<BannerVO> listBanners(int page, int size) {
        Page<Banner> p = bannerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder));
        Page<BannerVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toBannerVO).toList());
        return result;
    }

    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }

    /** Public: list active banners by position (no auth needed) */
    public List<BannerVO> listActiveBanners(String position) {
        long now = System.currentTimeMillis();
        List<Banner> list = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .eq(position != null, Banner::getPosition, position)
                .and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now))
                .and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now))
                .orderByAsc(Banner::getSortOrder));
        return list.stream().map(this::toBannerVO).toList();
    }

    // ---- Navigation ----

    public NavigationVO createNavigation(NavigationCreateDTO dto) {
        Navigation entity = new Navigation();
        entity.setName(dto.getName());
        entity.setIconUrl(dto.getIconUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        navigationMapper.insert(entity);
        return toNavVO(entity, null);
    }

    public NavigationVO updateNavigation(Long id, NavigationCreateDTO dto) {
        Navigation entity = requireNav(id);
        entity.setName(dto.getName());
        if (dto.getIconUrl() != null) entity.setIconUrl(dto.getIconUrl());
        if (dto.getLinkUrl() != null) entity.setLinkUrl(dto.getLinkUrl());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getParentId() != null) entity.setParentId(dto.getParentId());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        navigationMapper.updateById(entity);
        return toNavVO(entity, null);
    }

    public NavigationVO getNavigationById(Long id) {
        return toNavVO(requireNav(id), null);
    }

    public List<NavigationVO> navigationTree() {
        List<Navigation> all = navigationMapper.selectList(new LambdaQueryWrapper<Navigation>()
                .orderByAsc(Navigation::getSortOrder));
        Map<Long, List<Navigation>> byParent = all.stream()
                .collect(Collectors.groupingBy(n -> n.getParentId() == null ? 0L : n.getParentId()));
        return buildNavTree(byParent, 0L);
    }

    /** Public: active navigation tree */
    public List<NavigationVO> activeNavigationTree() {
        List<Navigation> all = navigationMapper.selectList(new LambdaQueryWrapper<Navigation>()
                .eq(Navigation::getStatus, 1)
                .orderByAsc(Navigation::getSortOrder));
        Map<Long, List<Navigation>> byParent = all.stream()
                .collect(Collectors.groupingBy(n -> n.getParentId() == null ? 0L : n.getParentId()));
        return buildNavTree(byParent, 0L);
    }

    public void deleteNavigation(Long id) {
        navigationMapper.deleteById(id);
    }

    // ---- private ----

    private List<NavigationVO> buildNavTree(Map<Long, List<Navigation>> byParent, Long parentId) {
        List<Navigation> children = byParent.getOrDefault(parentId, List.of());
        List<NavigationVO> result = new ArrayList<>();
        for (Navigation n : children) {
            List<NavigationVO> sub = buildNavTree(byParent, n.getId());
            result.add(toNavVO(n, sub.isEmpty() ? null : sub));
        }
        return result;
    }

    private PageTemplate requirePage(Long id) {
        PageTemplate e = pageTemplateMapper.selectById(id);
        if (e == null) throw new BizException("页面模板不存在");
        return e;
    }

    private Banner requireBanner(Long id) {
        Banner e = bannerMapper.selectById(id);
        if (e == null) throw new BizException("Banner不存在");
        return e;
    }

    private Navigation requireNav(Long id) {
        Navigation e = navigationMapper.selectById(id);
        if (e == null) throw new BizException("导航不存在");
        return e;
    }

    private PageTemplateVO toPageVO(PageTemplate e) {
        PageTemplateVO vo = new PageTemplateVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setTemplateType(e.getTemplateType());
        vo.setConfig(e.getConfig());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    private BannerVO toBannerVO(Banner e) {
        BannerVO vo = new BannerVO();
        vo.setId(e.getId());
        vo.setTitle(e.getTitle());
        vo.setImageUrl(e.getImageUrl());
        vo.setLinkUrl(e.getLinkUrl());
        vo.setPosition(e.getPosition());
        vo.setSortOrder(e.getSortOrder());
        vo.setStatus(e.getStatus());
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }

    private NavigationVO toNavVO(Navigation e, List<NavigationVO> children) {
        NavigationVO vo = new NavigationVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setIconUrl(e.getIconUrl());
        vo.setLinkUrl(e.getLinkUrl());
        vo.setSortOrder(e.getSortOrder());
        vo.setParentId(e.getParentId());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setChildren(children);
        return vo;
    }
}
