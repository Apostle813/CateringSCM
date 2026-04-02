package com.student.scm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustDTO {
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "食材ID不能为空")
    private Long materialId;

    @NotNull(message = "实际盘点数量不能为空")
    @Min(value = 0, message = "盘点数量不能小于0")
    private Integer realQty; // 库管员实际数出来的数量

    // 盘点原因
    private String reason;
}
