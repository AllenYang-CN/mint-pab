package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CashFlowVO {

    private String month;

    private BigDecimal totalInflow;

    private BigDecimal totalOutflow;

    private BigDecimal netFlow;

    private List<AccountFlowVO> accountFlows;

    @Data
    public static class AccountFlowVO {

        private Long accountId;

        private String accountName;

        private String accountType;

        private BigDecimal beginBalance;

        private BigDecimal inflow;

        private BigDecimal outflow;

        private BigDecimal netFlow;

        private BigDecimal endBalance;

    }

}
