package com.john.ecommerce.module.file.dto;

import lombok.Data;

@Data
public class FileUploadVO {
    private String url;
    private String objectKey;
    private String contentType;
    private long size;
}
