package com.john.ecommerce.module.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.merchant.dto.MerchantApplyDTO;
import com.john.ecommerce.module.merchant.dto.MerchantAuditDTO;
import com.john.ecommerce.module.merchant.dto.MerchantMeVO;
import com.john.ecommerce.module.merchant.dto.MerchantUpdateDTO;
import com.john.ecommerce.module.merchant.dto.MerchantVO;
import com.john.ecommerce.module.merchant.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.MERCHANT)
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/me")
    public R<MerchantMeVO> me() {
        return R.ok(merchantService.me());
    }

    @PostMapping("/apply")
    public R<MerchantVO> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        return R.ok(merchantService.apply(dto));
    }

    @PutMapping("/me")
    public R<MerchantVO> updateMe(@Valid @RequestBody MerchantUpdateDTO dto) {
        return R.ok(merchantService.updateProfile(dto));
    }

    @PutMapping("/{id}/audit")
    public R<MerchantVO> audit(@PathVariable Long id, @RequestBody MerchantAuditDTO dto) {
        return R.ok(merchantService.audit(id, dto));
    }

    @GetMapping("/{id:\\d+}")
    public R<MerchantVO> getById(@PathVariable Long id) {
        return R.ok(merchantService.getById(id));
    }

    @GetMapping
    public R<Page<MerchantVO>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) Integer status) {
        return R.ok(merchantService.list(page, size, status));
    }
}
