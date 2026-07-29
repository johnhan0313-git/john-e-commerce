package com.john.ecommerce.module.product.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.CategoryCreateDTO;
import com.john.ecommerce.module.product.dto.CategoryVO;
import com.john.ecommerce.module.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PRODUCT)
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public R<CategoryVO> create(@Valid @RequestBody CategoryCreateDTO dto) {
        return R.ok(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public R<CategoryVO> update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDTO dto) {
        return R.ok(categoryService.update(id, dto));
    }

    @GetMapping("/{id}")
    public R<CategoryVO> getById(@PathVariable Long id) {
        return R.ok(categoryService.getById(id));
    }

    @GetMapping("/tree")
    public R<List<CategoryVO>> tree() {
        return R.ok(categoryService.tree());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return R.ok();
    }
}
