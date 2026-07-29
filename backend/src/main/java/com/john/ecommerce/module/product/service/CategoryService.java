package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.CategoryCreateDTO;
import com.john.ecommerce.module.product.dto.CategoryVO;
import com.john.ecommerce.module.product.entity.Category;
import com.john.ecommerce.module.product.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryVO create(CategoryCreateDTO dto) {
        Category cat = new Category();
        cat.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        cat.setName(dto.getName());
        cat.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        cat.setLevel(dto.getLevel() != null ? dto.getLevel() : resolveLevel(cat.getParentId()));
        categoryMapper.insert(cat);
        return toVO(cat, null);
    }

    public CategoryVO update(Long id, CategoryCreateDTO dto) {
        Category cat = require(id);
        cat.setName(dto.getName());
        if (dto.getSortOrder() != null) cat.setSortOrder(dto.getSortOrder());
        if (dto.getParentId() != null) {
            cat.setParentId(dto.getParentId());
            cat.setLevel(resolveLevel(dto.getParentId()));
        }
        categoryMapper.updateById(cat);
        return toVO(cat, null);
    }

    public CategoryVO getById(Long id) {
        return toVO(require(id), null);
    }

    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
        Map<Long, List<Category>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        return buildTree(byParent, 0L);
    }

    public void delete(Long id) {
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException("存在子类目，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private List<CategoryVO> buildTree(Map<Long, List<Category>> byParent, Long parentId) {
        List<Category> children = byParent.getOrDefault(parentId, List.of());
        List<CategoryVO> result = new ArrayList<>();
        for (Category c : children) {
            List<CategoryVO> sub = buildTree(byParent, c.getId());
            result.add(toVO(c, sub.isEmpty() ? null : sub));
        }
        return result;
    }

    private int resolveLevel(Long parentId) {
        if (parentId == null || parentId == 0L) return 1;
        Category parent = categoryMapper.selectById(parentId);
        if (parent == null) throw new BizException("父类目不存在");
        return parent.getLevel() + 1;
    }

    private Category require(Long id) {
        Category cat = categoryMapper.selectById(id);
        if (cat == null) throw new BizException("类目不存在");
        return cat;
    }

    private CategoryVO toVO(Category c, List<CategoryVO> children) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setSortOrder(c.getSortOrder());
        vo.setLevel(c.getLevel());
        vo.setChildren(children);
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}
