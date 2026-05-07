package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_requisition_order")
@Schema(title="ScmRequisitionOrder对象", description="出库主表")
public class ScmRequisitionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private String orderNo;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "出库仓库ID")
    private Long warehouseId;

    @Schema(description = "状态(0:待审核 1:已配送出库)")
    private Integer status;

    @Schema(description = "状态(0:未结算 1:已结算)")
    private Integer paymentStatus;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "修改人")
    private Long updateBy;

}