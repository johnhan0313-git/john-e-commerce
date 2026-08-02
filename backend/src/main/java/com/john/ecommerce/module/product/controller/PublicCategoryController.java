package com.john.ecommerce.module.product.controller;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.product.dto.CategoryVO;
import com.john.ecommerce.module.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城前台公开类目（无需登录）。
 */
@RestController
@RequestMapping("/public/category")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public R<List<CategoryVO>> tree(@RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                                    @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        return R.ok(categoryService.tree());
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
