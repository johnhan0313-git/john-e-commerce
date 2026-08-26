package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.FxOrderCreateDTO;
import com.john.ecommerce.module.payment.dto.FxOrderVO;
import com.john.ecommerce.module.payment.service.CrossBorderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fx")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CROSSBORDER)
public class FxController {

    private final CrossBorderService crossBorderService;

    @PostMapping
    public R<FxOrderVO> create(@RequestBody FxOrderCreateDTO dto) {
        return R.ok(crossBorderService.createFxOrder(dto));
    }

    @GetMapping("/{id}")
    public R<FxOrderVO> getById(@PathVariable Long id) {
        return R.ok(crossBorderService.getFxOrder(id));
    }
}
