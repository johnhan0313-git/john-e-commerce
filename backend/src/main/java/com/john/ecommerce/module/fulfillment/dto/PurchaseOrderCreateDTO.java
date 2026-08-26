package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderCreateDTO {
    @NotNull(message = "供应商不能为空")
    private Long supplierId;
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;
    private Long refActivityId;
    private String remark;
    @NotEmpty(message = "采购明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "SKU不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        private Integer qty;
        @NotNull(message = "单价不能为空")
        private Long price;
    }
}
