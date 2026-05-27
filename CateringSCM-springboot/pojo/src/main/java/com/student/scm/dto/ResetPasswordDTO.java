package com.student.scm.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    /**
     * 登录账号（用户名）
     */
    private String username;

    /**
     * 注册时填写的手机号（用于身份验证）
     */
    private String phone;

    /**
     * 新密码
     */
    private String newPassword;
}
