package com.student.scm.dto;

import lombok.Data;

@Data
public class SupplierPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;     // 供应商名称（模糊查询）
    private String contact;  // 联系人（模糊查询）
    private Integer status;  // 状态（精准查询）
}
