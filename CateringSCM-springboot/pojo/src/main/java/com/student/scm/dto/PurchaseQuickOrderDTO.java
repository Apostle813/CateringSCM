package com.student.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseQuickOrderDTO {
    // 1. 主表信息
    private Long supplierId;     // 供应商ID
    private Long warehouseId;    // 入库仓库ID
    private Long materialId;
    private Integer planQty;
    private BigDecimal price;
}
