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

    // 如果以后老板说：“我要加个按供应商查询！”
    // 你只需要在这里加一行 private Long supplierId; 即可，接口定义完全不用动！
}
