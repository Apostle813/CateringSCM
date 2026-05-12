package com.student.scm.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScmRequisitionDetailVO {
    private Long id;
    private Long orderId;
    private Long materialId;
    private String materialName;
    private String category;
    private String unit;
    private Integer planQty;
    private Integer realQty;
}
