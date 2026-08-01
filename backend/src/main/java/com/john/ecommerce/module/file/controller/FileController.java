package com.john.ecommerce.module.file.controller;

import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.file.dto.FileUploadVO;
import com.john.ecommerce.module.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "folder", defaultValue = "misc") String folder) {
        return R.ok(fileStorageService.upload(file, folder));
    }
}
