package com.john.ecommerce.module.activity.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.activity.dto.PromoPreviewDTO;
import com.john.ecommerce.module.activity.service.ActivityService;
import com.john.ecommerce.module.activity.service.engine.PromoOrderResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promo")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.ACTIVITY)
public class PromoController {

    private final ActivityService activityService;

    @PostMapping("/preview")
    public R<PromoOrderResult> preview(@Valid @RequestBody PromoPreviewDTO dto) {
        return R.ok(activityService.preview(dto));
    }
}
