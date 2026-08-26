package com.john.ecommerce.module.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.merchant.dto.ShopApplyDTO;
import com.john.ecommerce.module.merchant.dto.ShopAuditDTO;
import com.john.ecommerce.module.merchant.dto.ShopUpdateDTO;
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
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('OPS')")
    public R<Page<ShopVO>> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) Long merchantId,
                                @RequestParam(required = false) Integer status) {
        return R.ok(shopService.list(page, size, merchantId, status));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('SELLER')")
    public R<List<ShopVO>> mine() {
        Merchant merchant = merchantService.requireApproved();
        return R.ok(shopService.listByMerchant(merchant.getId()));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('SELLER')")
    public R<ShopVO> apply(@Valid @RequestBody ShopApplyDTO dto) {
        Merchant merchant = merchantService.requireApproved();
        return R.ok(shopService.apply(merchant, dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('SELLER')")
    public R<ShopVO> update(@PathVariable Long id, @Valid @RequestBody ShopUpdateDTO dto) {
        Merchant merchant = merchantService.requireApproved();
        return R.ok(shopService.updateOwned(merchant, id, dto));
    }

    @PutMapping("/{id:\\d+}/audit")
    @PreAuthorize("hasRole('OPS')")
    public R<ShopVO> audit(@PathVariable Long id, @RequestBody ShopAuditDTO dto) {
        return R.ok(shopService.audit(id, dto));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('OPS')")
    public R<ShopVO> getById(@PathVariable Long id) {
        return R.ok(shopService.getById(id));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public R<Page<SpuVO>> listProducts(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(shopPortalService.listProducts(page, size, status));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public R<SpuVO> createProduct(@Valid @RequestBody SpuCreateDTO dto) {
        return R.ok(shopPortalService.createProduct(dto));
    }

    @PutMapping("/products/{id:\\d+}")
    @PreAuthorize("hasRole('SELLER')")
    public R<SpuVO> updateProduct(@PathVariable Long id, @Valid @RequestBody SpuCreateDTO dto) {
        return R.ok(shopPortalService.updateProduct(id, dto));
    }

    @PutMapping("/products/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public R<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        shopPortalService.updateProductStatus(id, status);
        return R.ok();
    }

    @GetMapping("/products/{spuId}/skus")
    @PreAuthorize("hasRole('SELLER')")
    public R<List<SkuVO>> listSkus(@PathVariable Long spuId) {
        return R.ok(shopPortalService.listSkus(spuId));
    }

    @PostMapping("/products/{spuId}/skus")
    @PreAuthorize("hasRole('SELLER')")
    public R<SkuVO> createSku(@PathVariable Long spuId, @Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(shopPortalService.createSku(spuId, dto));
    }

    @PutMapping("/skus/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public R<SkuVO> updateSku(@PathVariable Long id, @Valid @RequestBody SkuCreateDTO dto) {
        return R.ok(shopPortalService.updateSku(id, dto));
    }

    @DeleteMapping("/skus/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public R<Void> deleteSku(@PathVariable Long id) {
        shopPortalService.deleteSku(id);
        return R.ok();
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    public R<Page<OrderVO>> listOrders(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(shopPortalService.listOrders(page, size, status));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public R<OrderVO> getOrder(@PathVariable Long id) {
        return R.ok(shopPortalService.getOrder(id));
    }

    @PostMapping("/orders/{id}/ship")
    @PreAuthorize("hasRole('SELLER')")
    public R<LogisticsVO> ship(@PathVariable Long id, @Valid @RequestBody LogisticsCreateDTO dto) {
        return R.ok(shopPortalService.ship(id, dto));
    }

    @GetMapping("/settlements/bills")
    @PreAuthorize("hasRole('SELLER')")
    public R<Page<SettlementBillVO>> settlementBills(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return R.ok(shopPortalService.listSettlementBills(page, size));
    }

    @GetMapping("/settlements/orders")
    @PreAuthorize("hasRole('SELLER')")
    public R<Page<SettlementOrderVO>> settlementOrders(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return R.ok(shopPortalService.listSettlementOrders(page, size));
    }
}
