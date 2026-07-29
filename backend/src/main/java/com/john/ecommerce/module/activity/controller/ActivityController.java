package com.john.ecommerce.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.activity.dto.ActivityCreateDTO;
import com.john.ecommerce.module.activity.dto.ActivityVO;
import com.john.ecommerce.module.activity.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.ACTIVITY)
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public R<ActivityVO> create(@Valid @RequestBody ActivityCreateDTO dto) {
        return R.ok(activityService.create(dto));
    }

    @GetMapping("/{id}")
    public R<ActivityVO> getById(@PathVariable Long id) {
        return R.ok(activityService.getById(id));
    }

    @GetMapping
    public R<Page<ActivityVO>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) Integer status) {
        return R.ok(activityService.list(page, size, status));
    }

    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        activityService.publish(id);
        return R.ok();
    }

    @PutMapping("/{id}/offline")
    public R<Void> offline(@PathVariable Long id) {
        activityService.offline(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return R.ok();
    }
}
