package com.mint.pab.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetVO {

    private Long id;

    private String month;

    private String type;

    private String typeName;

    private Long categoryId;

    private String categoryName;

    private BigDecimal amount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
