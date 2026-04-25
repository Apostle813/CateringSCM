package com.student.scm.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ScmStockLogVO implements Serializable {

    private Long id;

    // 基础信息
    private String referenceNo;
    private LocalDateTime createTime;

    // 类型翻译
    private Integer type;
    private String typeName;

    // 仓库信息
    private Long warehouseId;
    private String warehouseName;

    // 物资信息
    private Long materialId;
    private String materialName;
    private String unit;

    // 数量变动
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;

    // 操作人信息
    private Long operatorId;
    private String operatorName;
}