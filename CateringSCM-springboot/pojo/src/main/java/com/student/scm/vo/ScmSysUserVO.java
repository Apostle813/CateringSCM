package com.student.scm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder   // 要能够在controller或者其他地方中快速构造对象，需要在这加上Builder构建器注解
public class ScmSysUserVO {
    private Long id;
    private String username;
    private String token;
}
