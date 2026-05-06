package com.mint.pab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class AccountDTO {

    private Long id;

    @NotBlank(message = "账户名称不能为空")
    @Size(min = 2, max = 30, message = "账户名称长度在2到30个字符之间")
    private String name;

    @NotBlank(message = "账户类型不能为空")
    private String type;

    @NotNull(message = "初始余额不能为空")
    private BigDecimal initialBalance;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    private String status;

}
