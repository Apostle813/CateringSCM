package com.student.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionOrderSubmitDTO {
    // 1. 主表信息
    private Long storeId;        // 门店ID
    private Long warehouseId;    // 出库仓库ID

    // 2. 明细列表
    private List<RequisitionDetailDTO> details;

    @Data
    public static class RequisitionDetailDTO {
        private Long materialId;      // 食材ID
        private Integer planQty;      // 计划出库数量
    }
}
