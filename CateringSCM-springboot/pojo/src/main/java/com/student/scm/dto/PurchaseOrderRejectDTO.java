package com.student.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderRejectDTO {
        @NotNull(message = "订单ID不能为空")
        private Long id;

        @NotBlank(message = "驳回原因不能为空")
        private String rejectReason;
}
