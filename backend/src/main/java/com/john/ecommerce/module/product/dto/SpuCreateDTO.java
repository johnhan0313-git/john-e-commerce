package com.john.ecommerce.module.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SpuCreateDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String subtitle;
    private Long categoryId;
    private Long merchantId;
    private Long shopId;
    private Long brandId;
    private String productCode;
    private List<String> mainImages;
    private String detail;
    private Integer productType;
    private Integer sortOrder;
    /** 销售规格定义，如 [{name:"颜色", values:["红","蓝"]}] */
    @Valid
    private List<SalesAttrDTO> salesAttrs;
    /** 创建时一并写入的 SKU 行（笛卡尔积结果） */
    @Valid
    private List<SkuItemDTO> skus;
}
