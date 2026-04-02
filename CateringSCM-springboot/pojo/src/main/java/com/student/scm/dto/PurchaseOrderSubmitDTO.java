package com.student.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderSubmitDTO {
    // 1. 主表信息
    private Long supplierId;     // 供应商ID
    private Long warehouseId;    // 入库仓库ID
    private String remark;       // 备注

    // 2. 明细列表 (这里是一个 List 集合！)
    private List<PurchaseDetailDTO> details;

    // 内部类：采购明细
    @Data
    public static class PurchaseDetailDTO {
        private Long materialId;      // 食材ID
        private Integer planQty;      // 计划采购数量
        private BigDecimal price;     // 采购单价
    }
}
