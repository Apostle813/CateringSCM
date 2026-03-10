package com.student.scm.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder   // 要能够在controller或者其他地方中快速构造对象，需要在这加上Builder构建器注解
public class ScmSysUserLoginDTO {

    private String username;
    private String password;
}
