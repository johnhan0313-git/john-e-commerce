package com.john.ecommerce.module.statistics.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.statistics.dto.OverviewVO;
import com.john.ecommerce.module.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.STATISTICS)
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public R<OverviewVO> overview() {
        return R.ok(statisticsService.overview());
    }
}
