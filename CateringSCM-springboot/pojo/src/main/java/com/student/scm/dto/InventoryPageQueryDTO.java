package com.student.scm.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryPageQueryDTO {
    // 1. 基础分页参数 (直接在这里赋上默认值，极度省事)
    @Min(value = 1, message = "页码至少为1")
    private Integer page = 1;
    @Min(value = 5, message = "分页大小至少为5")
    private Integer pageSize = 10;
    private Long warehouseId;
    private Long materialId;
}
