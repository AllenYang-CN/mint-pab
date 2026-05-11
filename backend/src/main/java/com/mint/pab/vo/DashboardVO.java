package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private BigDecimal todayIncome;

    private BigDecimal todayExpense;

    private Integer todayCount;

    private BigDecimal weekIncome;

    private BigDecimal weekExpense;

    private Integer weekCount;

    private BigDecimal weekDailyAvgExpense;

    private BigDecimal monthIncome;

    private BigDecimal monthExpense;

    private BigDecimal monthBalance;

    private BigDecimal budgetUsageRate;

    private String budgetStatus;

}
