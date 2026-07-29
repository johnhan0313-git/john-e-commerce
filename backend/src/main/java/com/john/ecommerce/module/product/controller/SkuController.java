package com.john.ecommerce.module.product.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.service.SkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sku")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PRODUCT)
public class SkuController {

    private final SkuService skuService;

    @PostMapping
    public R<SkuVO> create(@Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(skuService.create(dto));
    }

    @PutMapping("/{id}")
    public R<SkuVO> update(@PathVariable Long id, @Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(skuService.update(id, dto));
    }

    @GetMapping("/{id}")
    public R<SkuVO> getById(@PathVariable Long id) {
        return R.ok(skuService.getById(id));
    }

    @GetMapping
    public R<List<SkuVO>> listBySpu(@RequestParam Long spuId) {
        return R.ok(skuService.listBySpu(spuId));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        skuService.delete(id);
        return R.ok();
    }
}
