package com.student.scm.dto;

import lombok.Data;

@Data
public class ScmSysUserPageQueryDTO {
    private int page = 1;
    private int pageSize = 10;
    private String username;
    private String realName;
    private Integer status;
}
