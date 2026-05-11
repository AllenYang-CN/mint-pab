package com.mint.pab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BudgetDTO {

    private Long id;

    @NotBlank(message = "预算月份不能为空")
    private String month;

    @NotBlank(message = "预算类型不能为空")
    private String type;

    private Long categoryId;

    @NotNull(message = "预算金额不能为空")
    private BigDecimal amount;

}
