package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BudgetExecutionVO {

    private String month;

    private BigDecimal totalBudget;

    private BigDecimal totalUsed;

    private BigDecimal totalRemaining;

    private BigDecimal totalUsageRate;

    private String totalStatus;

    private List<CategoryBudgetVO> categoryBudgets;

    @Data
    public static class CategoryBudgetVO {

        private Long categoryId;

        private String categoryParentName;

        private BigDecimal budgetAmount;

        private BigDecimal usedAmount;

        private BigDecimal remainingAmount;

        private BigDecimal usageRate;

        private String status;

    }

}
