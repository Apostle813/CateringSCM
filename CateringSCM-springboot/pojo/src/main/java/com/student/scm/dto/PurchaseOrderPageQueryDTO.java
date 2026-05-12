package com.student.scm.dto;

import lombok.Data;

@Data
public class PurchaseOrderPageQueryDTO {
    // 1. 基础分页参数 (直接在这里赋上默认值，极度省事)
    private Integer page = 1;
    private Integer pageSize = 10;

    // 2. 具体的业务过滤条件
    private String orderNo;  // 按单号模糊查
    private Integer status;  // 按状态精准查
    
    // 新增筛选字段
    private String startDate;  // 开始日期
    private String endDate;    // 结束日期
    private Long supplierId;   // 供应商ID
    private Long warehouseId; // 仓库ID
}
