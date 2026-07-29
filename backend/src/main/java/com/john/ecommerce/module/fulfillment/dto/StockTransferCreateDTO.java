package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockTransferCreateDTO {
    @NotNull(message = "源仓库不能为空")
    private Long fromWarehouseId;
    @NotNull(message = "目标仓库不能为空")
    private Long toWarehouseId;
    private String remark;
    @NotEmpty(message = "调拨明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "SKU不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        private Integer qty;
    }
}
