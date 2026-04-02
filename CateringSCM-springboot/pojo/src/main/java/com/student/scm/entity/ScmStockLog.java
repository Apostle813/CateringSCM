package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_stock_log")
@Schema(title="ScmStockLog对象", description="库存流水日志表")
public class ScmStockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联单据号(采购单号/领料单号)")
    private String referenceNo;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "类型(1:采购入库 2:领料出库 3:盘点调整)")
    private Integer type;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "变动数量(+100 或 -50)")
    private Integer changeQty;

    @Schema(description = "变动前数量")
    private Integer beforeQty;

    @Schema(description = "变动后数量")
    private Integer afterQty;

    @Schema(description = "发生时间")
    private LocalDateTime createTime;

}
