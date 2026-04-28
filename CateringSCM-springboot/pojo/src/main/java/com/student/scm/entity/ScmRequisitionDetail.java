package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_requisition_detail")
@Schema(title="ScmRequisitionDetail对象", description="出库明细表")
public class ScmRequisitionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "请购出库单ID")
    private Long orderId;

    @Schema(description = "食材ID")
    private Long materialId;

    @Schema(description = "请购出库数量")
    private Integer planQty;

    @Schema(description = "实际出库数量")
    private Integer realQty;
}
