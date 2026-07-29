package com.john.ecommerce.module.content.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.content.dto.NavigationCreateDTO;
import com.john.ecommerce.module.content.dto.NavigationVO;
import com.john.ecommerce.module.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/content/navigation")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CONTENT)
public class NavigationController {

    private final ContentService contentService;

    @PostMapping
    public R<NavigationVO> create(@Valid @RequestBody NavigationCreateDTO dto) {
        return R.ok(contentService.createNavigation(dto));
    }

    @PutMapping("/{id}")
    public R<NavigationVO> update(@PathVariable Long id, @Valid @RequestBody NavigationCreateDTO dto) {
        return R.ok(contentService.updateNavigation(id, dto));
    }

    @GetMapping("/{id}")
    public R<NavigationVO> getById(@PathVariable Long id) {
        return R.ok(contentService.getNavigationById(id));
    }

    @GetMapping("/tree")
    public R<List<NavigationVO>> tree() {
        return R.ok(contentService.navigationTree());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        contentService.deleteNavigation(id);
        return R.ok();
    }
}
