package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_inventory")
@Schema(title="ScmInventory对象", description="实时库存表")
public class ScmInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "当前库存")
    private Integer quantity;

    @Schema(description = "首次入库时间")
    private LocalDateTime createTime;

    @Schema(description = "库存更新时间")
    private LocalDateTime updateTime;


}
