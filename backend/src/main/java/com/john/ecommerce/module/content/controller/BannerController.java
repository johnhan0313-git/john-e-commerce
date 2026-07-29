package com.john.ecommerce.module.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.content.dto.BannerCreateDTO;
import com.john.ecommerce.module.content.dto.BannerVO;
import com.john.ecommerce.module.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/banner")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CONTENT)
public class BannerController {

    private final ContentService contentService;

    @PostMapping
    public R<BannerVO> create(@Valid @RequestBody BannerCreateDTO dto) {
        return R.ok(contentService.createBanner(dto));
    }

    @PutMapping("/{id}")
    public R<BannerVO> update(@PathVariable Long id, @Valid @RequestBody BannerCreateDTO dto) {
        return R.ok(contentService.updateBanner(id, dto));
    }

    @GetMapping("/{id}")
    public R<BannerVO> getById(@PathVariable Long id) {
        return R.ok(contentService.getBannerById(id));
    }

    @GetMapping
    public R<Page<BannerVO>> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return R.ok(contentService.listBanners(page, size));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        contentService.deleteBanner(id);
        return R.ok();
    }
}
