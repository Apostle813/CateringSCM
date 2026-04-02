package com.student.scm.dto;

import lombok.Data;

@Data
public class MaterialPageQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;
}
