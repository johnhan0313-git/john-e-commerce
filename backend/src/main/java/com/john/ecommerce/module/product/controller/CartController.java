package com.john.ecommerce.module.product.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.CartAddDTO;
import com.john.ecommerce.module.product.dto.CartVO;
import com.john.ecommerce.module.product.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PRODUCT)
public class CartController {

    private final CartService cartService;

    @PostMapping
    public R<CartVO> add(@Valid @RequestBody CartAddDTO dto) {
        return R.ok(cartService.add(dto));
    }

    @PutMapping("/{id}/quantity")
    public R<CartVO> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        return R.ok(cartService.updateQuantity(id, quantity));
    }

    @PutMapping("/{id}/selected")
    public R<Void> updateSelected(@PathVariable Long id, @RequestParam Integer selected) {
        cartService.updateSelected(id, selected);
        return R.ok();
    }

    @GetMapping
    public R<List<CartVO>> list() {
        return R.ok(cartService.listMine());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return R.ok();
    }

    @DeleteMapping
    public R<Void> clear() {
        cartService.clear();
        return R.ok();
    }
}
