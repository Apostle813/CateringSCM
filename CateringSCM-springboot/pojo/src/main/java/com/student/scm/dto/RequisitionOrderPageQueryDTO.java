package com.student.scm.dto;

import lombok.Data;

@Data
public class RequisitionOrderPageQueryDTO {
    private int page = 1;
    private int pageSize = 10;
    private String orderNo;
    private Integer status;
}
