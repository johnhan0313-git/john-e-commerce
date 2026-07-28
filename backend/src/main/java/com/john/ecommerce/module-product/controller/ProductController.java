package com.john.ecommerce.module-product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module-product.dto.SpuCreateDTO;
import com.john.ecommerce.module-product.dto.SpuVO;
import com.john.ecommerce.module-product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public R<SpuVO> create(@Valid @RequestBody SpuCreateDTO dto) {
        return R.ok(productService.create(dto));
    }

    @GetMapping("/{id}")
    public R<SpuVO> getById(@PathVariable Long id) {
        return R.ok(productService.getById(id));
    }

    @GetMapping
    public R<Page<SpuVO>> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) Integer status) {
        return R.ok(productService.list(page, size, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return R.ok();
    }
}
