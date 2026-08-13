package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.CategoryCreateDTO;
import com.john.ecommerce.module.product.dto.CategoryVO;
import com.john.ecommerce.module.product.entity.Category;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.CategoryMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    /** 业界常见大/中/小类 3 级；更深不利于前台导航与运营维护 */
    public static final int MAX_LEVEL = 3;

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;

    public CategoryVO create(CategoryCreateDTO dto) {
        Category cat = new Category();
        cat.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        cat.setName(dto.getName());
        cat.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        int level = resolveLevel(cat.getParentId());
        assertLevelAllowed(level);
        cat.setLevel(level);
        categoryMapper.insert(cat);
        return toVO(cat, null);
    }

    public CategoryVO update(Long id, CategoryCreateDTO dto) {
        Category cat = require(id);
        cat.setName(dto.getName());
        if (dto.getSortOrder() != null) cat.setSortOrder(dto.getSortOrder());
        if (dto.getParentId() != null) {
            Long newParentId = dto.getParentId();
            Long oldParentId = cat.getParentId() == null ? 0L : cat.getParentId();
            if (!newParentId.equals(oldParentId)) {
                if (newParentId.equals(id)) {
                    throw new BizException("不能将类目设为自己的父级");
                }
                Long childCount = categoryMapper.selectCount(
                        new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
                if (childCount != null && childCount > 0) {
                    throw new BizException("存在子类目时不可调整父级");
                }
                int newLevel = resolveLevel(newParentId);
                assertLevelAllowed(newLevel);
                cat.setParentId(newParentId);
                cat.setLevel(newLevel);
            }
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
        Long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getCategoryId, id));
        if (spuCount != null && spuCount > 0) {
            throw new BizException("类目下仍有商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    /** 自身 + 全部子孙类目 id（用于商品筛选） */
    public List<Long> selfAndDescendantIds(Long categoryId) {
        require(categoryId);
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getId));
        Map<Long, List<Category>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        List<Long> ids = new ArrayList<>();
        collectIds(byParent, categoryId, ids);
        return ids;
    }

    private void collectIds(Map<Long, List<Category>> byParent, Long id, List<Long> out) {
        out.add(id);
        for (Category c : byParent.getOrDefault(id, List.of())) {
            collectIds(byParent, c.getId(), out);
        }
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
        Integer parentLevel = parent.getLevel();
        return (parentLevel != null ? parentLevel : 1) + 1;
    }

    private void assertLevelAllowed(int level) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new BizException("类目最多支持 " + MAX_LEVEL + " 级（当前为第 " + level + " 级）");
        }
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
