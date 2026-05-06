package com.mint.pab.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AccountType {

    CASH(1, "现金"),
    BANK_SAVINGS(2, "银行储蓄卡"),
    CREDIT_CARD(3, "信用卡"),
    ALIPAY(4, "支付宝"),
    WECHAT(5, "微信");

    private final Integer code;
    private final String desc;

    public static AccountType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static AccountType getByName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
