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
@TableName("scm_supplier")
@Schema(title="BaseSupplier对象", description="供应商表")
public class ScmSupplier implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商名称")
    private String name;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "状态")
    private Integer status;

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
