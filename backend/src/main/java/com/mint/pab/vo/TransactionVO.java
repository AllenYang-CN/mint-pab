package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionVO {

    private Long id;

    private String type;

    private String typeName;

    private Long fromAccountId;

    private String fromAccountName;

    private Long toAccountId;

    private String toAccountName;

    private BigDecimal amount;

    private Long categoryId;

    private String categoryParentName;

    private String categoryName;

    private String transactionTime;

    private String remark;

    private String createTime;

}
