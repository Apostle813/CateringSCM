package com.student.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("scm_sys_user")
@Schema(title="ScmSysUser", description="用户表")
public class ScmSysUser {
    @Schema(description = "主键用户ID")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录密码")
    private String password; // 实际开发中密码不能明文传输，需加密

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "关联的角色ID")
    private Long roleId;     // 绑定角色，决定他能干什么

    @Schema(description = "账号状态(1:启用 0:停用)")
    private Integer status;

    @Schema(description = "逻辑删除标识(0正常,1删除)")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
