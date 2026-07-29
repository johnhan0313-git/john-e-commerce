package com.john.ecommerce.module.product.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.PriceRuleCreateDTO;
import com.john.ecommerce.module.product.dto.PriceRuleVO;
import com.john.ecommerce.module.product.service.PriceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/price-rule")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PRODUCT)
public class PriceRuleController {

    private final PriceRuleService priceRuleService;

    @PostMapping
    public R<PriceRuleVO> create(@Valid @RequestBody PriceRuleCreateDTO dto) {
        return R.ok(priceRuleService.create(dto));
    }

    @PutMapping("/{id}")
    public R<PriceRuleVO> update(@PathVariable Long id, @Valid @RequestBody PriceRuleCreateDTO dto) {
        return R.ok(priceRuleService.update(id, dto));
    }

    @GetMapping("/{id}")
    public R<PriceRuleVO> getById(@PathVariable Long id) {
        return R.ok(priceRuleService.getById(id));
    }

    @GetMapping
    public R<List<PriceRuleVO>> list(@RequestParam(required = false) Long spuId,
                                     @RequestParam(required = false) Long skuId) {
        return R.ok(priceRuleService.list(spuId, skuId));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        priceRuleService.delete(id);
        return R.ok();
    }
}
