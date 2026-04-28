package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_sys_role")
@Schema(title="ScmSysRole对象", description="角色表")
public class ScmSysRole {

    @Schema(description = "主键用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色权限")
    private String roleCode; // 实际开发中密码不能明文传输，需加密

    @Schema(description = "角色职责描述")
    private String description;
}
