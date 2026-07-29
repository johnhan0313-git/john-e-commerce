package com.john.ecommerce.module.activity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PromoPreviewDTO {
    @NotEmpty(message = "商品行不能为空")
    @Valid
    private List<LineDTO> lines;

    @Data
    public static class LineDTO {
        @NotNull(message = "SKU ID 不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
