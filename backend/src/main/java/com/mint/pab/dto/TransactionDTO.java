package com.mint.pab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class TransactionDTO {

    private Long id;

    @NotBlank(message = "交易类型不能为空")
    private String type;

    private Long fromAccountId;

    private Long toAccountId;

    @NotNull(message = "交易金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "交易时间不能为空")
    private String transactionTime;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
