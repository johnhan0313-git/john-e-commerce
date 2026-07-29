package com.john.ecommerce.module.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.BrandCreateDTO;
import com.john.ecommerce.module.product.dto.BrandVO;
import com.john.ecommerce.module.product.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PRODUCT)
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public R<BrandVO> create(@Valid @RequestBody BrandCreateDTO dto) {
        return R.ok(brandService.create(dto));
    }

    @PutMapping("/{id}")
    public R<BrandVO> update(@PathVariable Long id, @Valid @RequestBody BrandCreateDTO dto) {
        return R.ok(brandService.update(id, dto));
    }

    @GetMapping("/{id}")
    public R<BrandVO> getById(@PathVariable Long id) {
        return R.ok(brandService.getById(id));
    }

    @GetMapping
    public R<Page<BrandVO>> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(brandService.list(page, size, status));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return R.ok();
    }
}
