package com.student.scm.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_purchase_detail")
@Schema(title="ScmPurchaseDetail对象", description="采购订单明细")
public class ScmPurchaseDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "计划采购数")
    private Integer planQty;

    @Schema(description = "实际入库数(ERP允许实收与计划不同)")
    private Integer realQty;

    @Schema(description = "采购单价")
    private BigDecimal price;


}
