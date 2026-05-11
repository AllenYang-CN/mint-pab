package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IncomeExpenseVO {

    private String month;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal balance;

    private List<CategoryAmountVO> incomeItems;

    private List<CategoryAmountVO> expenseItems;

    @Data
    public static class CategoryAmountVO {

        private String categoryParentName;

        private String categoryName;

        private BigDecimal amount;

        private BigDecimal percentage;

    }

}
