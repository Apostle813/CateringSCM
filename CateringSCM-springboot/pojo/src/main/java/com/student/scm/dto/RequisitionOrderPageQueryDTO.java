package com.student.scm.dto;

import lombok.Data;

@Data
public class RequisitionOrderPageQueryDTO {
    private int page = 1;
    private int pageSize = 10;
    private String orderNo;
    private Integer status;
    
    // 新增筛选字段
    private String startDate;  // 开始日期
    private String endDate;    // 结束日期
    private Long warehouseId;  // 仓库ID
}
