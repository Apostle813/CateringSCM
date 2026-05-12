package com.student.scm.dto;

import lombok.Data;

@Data
public class StockLogPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String referenceNo;
    
    // 新增筛选字段
    private Integer type;        // 类型(1:采购入库 2:领料出库 3:盘点调整)
    private String startDate;    // 开始日期
    private String endDate;      // 结束日期
    private Long warehouseId;    // 仓库ID
    private Long materialId;     // 物料ID
    private String materialName;  // 物料名称(关联物料表筛选)
}
