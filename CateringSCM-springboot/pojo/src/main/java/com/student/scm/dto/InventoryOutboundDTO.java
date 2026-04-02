package com.student.scm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryOutboundDTO {
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "食材ID不能为空")
    private Long materialId;

    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量至少为1")
    private Integer outQty;

    // 领料用途或领料单号（选填，用于记流水）
    private String referenceNo;
}
