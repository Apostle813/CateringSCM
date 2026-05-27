package com.student.scm.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    /**
     * 被修改密码的用户ID，管理员修改他人密码时传，
     * 不传或传自己时走"验证旧密码"流程
     */
    private Long userId;

    /**
     * 旧密码（非管理员必填，用于验证身份）
     */
    private String oldPassword;

    /**
     * 新密码（必填）
     */
    private String newPassword;
}
