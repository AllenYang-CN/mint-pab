package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BalanceSheetVO {

    private BigDecimal totalAssets;

    private List<AccountGroupVO> accountGroups;

    @Data
    public static class AccountGroupVO {

        private String type;

        private String typeName;

        private BigDecimal totalBalance;

        private List<AccountItemVO> accounts;

    }

    @Data
    public static class AccountItemVO {

        private Long id;

        private String name;

        private String type;

        private BigDecimal currentBalance;

    }

}
