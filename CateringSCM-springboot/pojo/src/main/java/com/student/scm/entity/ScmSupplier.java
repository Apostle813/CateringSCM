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
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;


}
