package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_purchase_order")
@Schema(title="ScmPurchaseOrder对象", description="采购订单主表")
public class ScmPurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "单号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "入库仓库ID")
    private Long warehouseId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建用户")
    private long createUser;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态(0:待审核 1:审核通过 2:已入库 9:驳回)")
    private Integer status;

    @Schema(description = "财务结算状态(0:未结算 1:已结算)")
    private Integer paymentStatus;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核人ID")
    private Long auditUser;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "逻辑删除")
    private Integer isDeleted;


}
