package com.student.scm.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ScmInventoryVO implements Serializable {

    private Long id;

    // 仓库信息
    private Long warehouseId;
    private String warehouseName;

    // 物资基础信息（从 ScmMaterial 关联获取）
    private Long materialId;
    private String materialName;
    private String category;     // 食材分类
    private String unit;         // 单位
    private String spec;         // 规格说明
    private BigDecimal price;    // 参考单价

    // 库存数据
    private Integer quantity; // 当前库存量

}
