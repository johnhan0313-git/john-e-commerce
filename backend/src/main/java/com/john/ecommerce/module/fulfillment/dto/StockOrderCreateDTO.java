package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockOrderCreateDTO {
    @NotBlank(message = "单据类型不能为空")
    private String orderType;
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;
    private String refNo;
    private String remark;
    @NotEmpty(message = "明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "SKU不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        private Integer qty;
        private String lotNo;
    }
}
