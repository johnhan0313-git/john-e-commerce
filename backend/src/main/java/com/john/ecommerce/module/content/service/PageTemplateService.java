package com.john.ecommerce.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.content.entity.PageTemplate;
import com.john.ecommerce.module.content.mapper.PageTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageTemplateService {

    private final PageTemplateMapper pageTemplateMapper;

    public PageTemplate create(PageTemplate entity) {
        pageTemplateMapper.insert(entity);
        return entity;
    }

    public PageTemplate update(Long id, PageTemplate entity) {
        PageTemplate existing = require(id);
        existing.setName(entity.getName());
        existing.setTemplateType(entity.getTemplateType());
        existing.setConfig(entity.getConfig());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        pageTemplateMapper.updateById(existing);
        return existing;
    }

    public PageTemplate getById(Long id) {
        return require(id);
    }

    public Page<PageTemplate> list(int page, int size) {
        return pageTemplateMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PageTemplate>().orderByDesc(PageTemplate::getCreatedAt));
    }

    public void delete(Long id) {
        require(id);
        pageTemplateMapper.deleteById(id);
    }

    private PageTemplate require(Long id) {
        PageTemplate t = pageTemplateMapper.selectById(id);
        if (t == null) throw new BizException("页面模板不存在");
        return t;
    }
}
