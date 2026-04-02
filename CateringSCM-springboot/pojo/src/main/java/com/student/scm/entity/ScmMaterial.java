package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("scm_material")
@Schema(title="BaseMaterial对象", description="物料表")
public class ScmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "物料名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "描述")
    private String spec;

    @Schema(description = "参考价")
    private BigDecimal price;

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
