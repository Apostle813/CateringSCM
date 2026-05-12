package com.student.scm.dto;

import lombok.Data;

@Data
public class OperationLogPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 15;
    private String operationType;      // 操作类型筛选
    private String operatorName;       // 操作人姓名模糊查询
    private String startDate;          // 开始日期
    private String endDate;            // 结束日期
    private String targetType;         // 业务对象类型
}
