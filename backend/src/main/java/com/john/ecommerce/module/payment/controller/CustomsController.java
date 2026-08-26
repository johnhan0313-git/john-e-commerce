package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.CustomsDeclareDTO;
import com.john.ecommerce.module.payment.dto.CustomsDeclarationVO;
import com.john.ecommerce.module.payment.service.CrossBorderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customs")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CROSSBORDER)
public class CustomsController {

    private final CrossBorderService crossBorderService;

    @PostMapping("/declare")
    public R<CustomsDeclarationVO> declare(@RequestBody CustomsDeclareDTO dto) {
        return R.ok(crossBorderService.declare(dto));
    }

    @GetMapping("/{id}")
    public R<CustomsDeclarationVO> getById(@PathVariable Long id) {
        return R.ok(crossBorderService.getDeclaration(id));
    }
}
