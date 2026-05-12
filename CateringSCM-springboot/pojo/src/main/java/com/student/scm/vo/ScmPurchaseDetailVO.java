package com.student.scm.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ScmPurchaseDetailVO {
    private Long id;
    private Long orderId;
    private Long materialId;
    private String materialName;
    private String category;
    private String unit;
    private Integer planQty;
    private Integer realQty;
    private BigDecimal price;
    private BigDecimal lineAmount; // 小计 = planQty * price
}
