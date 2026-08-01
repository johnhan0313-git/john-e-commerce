package com.john.ecommerce.module.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.product.service.ProductService;
import com.john.ecommerce.module.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城前台公开商品接口（无需登录）。
 * SecurityConfig 已 permitAll /public/**；需 X-Tenant-Id 或 tenantId。
 */
@RestController
@RequestMapping("/public/product")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;
    private final SkuService skuService;

    @GetMapping
    public R<Page<SpuVO>> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                               @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        return R.ok(productService.list(page, size, 1));
    }

    @GetMapping("/{id}")
    public R<SpuVO> getById(@PathVariable Long id,
                            @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                            @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        SpuVO vo = productService.getById(id);
        if (vo.getStatus() == null || vo.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        return R.ok(vo);
    }

    @GetMapping("/{id}/skus")
    public R<List<SkuVO>> listSkus(@PathVariable Long id,
                                   @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                                   @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        SpuVO vo = productService.getById(id);
        if (vo.getStatus() == null || vo.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        List<SkuVO> skus = skuService.listBySpu(id).stream()
                .filter(s -> s.getStatus() == null || s.getStatus() == 1)
                .toList();
        return R.ok(skus);
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
