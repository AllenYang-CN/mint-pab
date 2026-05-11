package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountVO {

    private Long id;

    private String name;

    private String type;

    private String typeName;

    private BigDecimal initialBalance;

    private BigDecimal currentBalance;

    private String remark;

    private String status;

    private String statusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
