package com.student.scm.dto;

import lombok.Data;

@Data
public class MaterialPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;       // 按名称模糊查询
    private String category;   // 按分类筛选
    private String unit;       // 按单位筛选
}
