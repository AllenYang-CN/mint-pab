package com.mint.pab.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionQueryDTO {

    /** 开始日期，格式 yyyy-MM-dd */
    private String startDate;

    /** 结束日期，格式 yyyy-MM-dd */
    private String endDate;

    /** 交易类型，逗号分隔，如 INCOME,EXPENSE */
    private String types;

    /** 账户ID，逗号分隔，如 1,2,3 */
    private String accountIds;

    /** 分类ID，逗号分隔，如 4,5,6 */
    private String categoryIds;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 20;

}
