package com.john.ecommerce.module.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.merchant.dto.ShopVO;
import com.john.ecommerce.module.merchant.service.ShopService;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商城前台公开店铺接口（无需登录）。
 * SecurityConfig 已 permitAll /public/**；需 X-Tenant-Id 或 tenantId。
 */
@RestController
@RequestMapping("/public/shop")
@RequiredArgsConstructor
public class PublicShopController {

    private final ShopService shopService;
    private final ProductService productService;

    @GetMapping("/{id}")
    public R<ShopVO> getById(@PathVariable Long id,
                             @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                             @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        ShopVO shop = shopService.getById(id);
        if (shop.getStatus() == null || shop.getStatus() != ShopService.STATUS_OPEN) {
            throw new BizException("店铺不存在或未营业");
        }
        return R.ok(shop);
    }

    @GetMapping("/{id}/products")
    public R<Page<SpuVO>> listProducts(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                                       @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        ShopVO shop = shopService.getById(id);
        if (shop.getStatus() == null || shop.getStatus() != ShopService.STATUS_OPEN) {
            throw new BizException("店铺不存在或未营业");
        }
        return R.ok(productService.list(page, size, 1, id, null));
    }

    private void setTenantIfAbsent(Long header, Long param) {
        if (TenantContext.getTenantId() == null) {
            Long tid = header != null ? header : param;
            if (tid == null) {
                throw new BizException(400, "缺少租户上下文（X-Tenant-Id）");
            }
            TenantContext.setTenantId(tid);
        }
    }
}
