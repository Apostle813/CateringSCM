package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("scm_warehouse")
@Schema(title="BaseWarehouse对象", description="仓库表")
public class ScmWarehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "仓库名称(如:主仓库,冷冻库)")
    private String name;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "负责人")
    private String manager;

    @Schema(description = "逻辑删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;


}
