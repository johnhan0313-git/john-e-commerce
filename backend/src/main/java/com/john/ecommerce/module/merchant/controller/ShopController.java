package com.john.ecommerce.module.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.merchant.dto.ShopApplyDTO;
import com.john.ecommerce.module.merchant.dto.ShopAuditDTO;
import com.john.ecommerce.module.merchant.dto.ShopVO;
import com.john.ecommerce.module.merchant.entity.Merchant;
import com.john.ecommerce.module.merchant.service.MerchantService;
import com.john.ecommerce.module.merchant.service.ShopPortalService;
import com.john.ecommerce.module.merchant.service.ShopService;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.dto.SpuCreateDTO;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.trade.dto.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.MERCHANT)
public class ShopController {

    private final ShopService shopService;
    private final ShopPortalService shopPortalService;
    private final MerchantService merchantService;

    @GetMapping
    public R<Page<ShopVO>> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) Long merchantId,
                                @RequestParam(required = false) Integer status) {
        return R.ok(shopService.list(page, size, merchantId, status));
    }

    @GetMapping("/mine")
    public R<List<ShopVO>> mine() {
        Merchant merchant = merchantService.requireApproved();
        return R.ok(shopService.listByMerchant(merchant.getId()));
    }

    @PostMapping("/apply")
    public R<ShopVO> apply(@Valid @RequestBody ShopApplyDTO dto) {
        Merchant merchant = merchantService.requireApproved();
        return R.ok(shopService.apply(merchant, dto));
    }

    @PutMapping("/{id:\\d+}/audit")
    public R<ShopVO> audit(@PathVariable Long id, @RequestBody ShopAuditDTO dto) {
        return R.ok(shopService.audit(id, dto));
    }

    @GetMapping("/{id:\\d+}")
    public R<ShopVO> getById(@PathVariable Long id) {
        return R.ok(shopService.getById(id));
    }

    @GetMapping("/products")
    public R<Page<SpuVO>> listProducts(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(shopPortalService.listProducts(page, size, status));
    }

    @PostMapping("/products")
    public R<SpuVO> createProduct(@Valid @RequestBody SpuCreateDTO dto) {
        return R.ok(shopPortalService.createProduct(dto));
    }

    @PutMapping("/products/{id}/status")
    public R<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        shopPortalService.updateProductStatus(id, status);
        return R.ok();
    }

    @GetMapping("/products/{spuId}/skus")
    public R<List<SkuVO>> listSkus(@PathVariable Long spuId) {
        return R.ok(shopPortalService.listSkus(spuId));
    }

    @PostMapping("/products/{spuId}/skus")
    public R<SkuVO> createSku(@PathVariable Long spuId, @Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(shopPortalService.createSku(spuId, dto));
    }

    @DeleteMapping("/skus/{id}")
    public R<Void> deleteSku(@PathVariable Long id) {
        shopPortalService.deleteSku(id);
        return R.ok();
    }

    @GetMapping("/orders")
    public R<Page<OrderVO>> listOrders(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(shopPortalService.listOrders(page, size, status));
    }

    @GetMapping("/orders/{id}")
    public R<OrderVO> getOrder(@PathVariable Long id) {
        return R.ok(shopPortalService.getOrder(id));
    }

    @PostMapping("/orders/{id}/ship")
    public R<LogisticsVO> ship(@PathVariable Long id, @Valid @RequestBody LogisticsCreateDTO dto) {
        return R.ok(shopPortalService.ship(id, dto));
    }
}
