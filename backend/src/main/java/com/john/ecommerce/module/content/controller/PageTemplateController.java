package com.john.ecommerce.module.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.content.dto.PageTemplateCreateDTO;
import com.john.ecommerce.module.content.dto.PageTemplateVO;
import com.john.ecommerce.module.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/page")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CONTENT)
public class PageTemplateController {

    private final ContentService contentService;

    @PostMapping
    public R<PageTemplateVO> create(@Valid @RequestBody PageTemplateCreateDTO dto) {
        return R.ok(contentService.createPage(dto));
    }

    @PutMapping("/{id}")
    public R<PageTemplateVO> update(@PathVariable Long id, @Valid @RequestBody PageTemplateCreateDTO dto) {
        return R.ok(contentService.updatePage(id, dto));
    }

    @GetMapping("/{id}")
    public R<PageTemplateVO> getById(@PathVariable Long id) {
        return R.ok(contentService.getPageById(id));
    }

    @GetMapping
    public R<Page<PageTemplateVO>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(contentService.listPages(page, size));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        contentService.deletePage(id);
        return R.ok();
    }
}
