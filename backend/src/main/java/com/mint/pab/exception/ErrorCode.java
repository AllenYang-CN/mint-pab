package com.mint.pab.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),

    LOGIN_FAILED(10010001, "用户名或密码错误"),
    ACCOUNT_LOCKED(10010002, "账户已锁定，请30分钟后重试"),
    TOKEN_EXPIRED(10010003, "Token已过期，请重新登录"),
    TOKEN_INVALID(10010004, "Token无效，请重新登录"),

    ACCOUNT_NAME_DUPLICATE(30020001, "账户名称已存在"),
    ACCOUNT_HAS_TRANSACTION(30020002, "该账户存在交易记录，不允许删除"),
    ACCOUNT_DISABLED(30020003, "该账户已停用"),
    ACCOUNT_TYPE_INVALID(30020004, "账户类型不合法"),
    ACCOUNT_STATUS_INVALID(30020005, "账户状态值不合法"),

    TRANSACTION_AMOUNT_INVALID(40030001, "交易金额必须大于0"),
    BALANCE_INSUFFICIENT(40030002, "账户余额不足"),
    SAME_ACCOUNT_TRANSFER(40030003, "转出账户和转入账户不能相同"),
    TRANSACTION_TYPE_INVALID(40030004, "交易类型不合法"),

    CATEGORY_HAS_TRANSACTION(50040001, "该分类下存在交易记录，不允许删除"),
    SYSTEM_CATEGORY_READONLY(50040002, "系统预置分类不允许修改或删除"),
    CATEGORY_TYPE_INVALID(50040003, "分类类型不合法"),
    PARENT_CATEGORY_NOT_FOUND(50040004, "父分类不存在"),
    CATEGORY_HAS_CHILDREN(50040005, "该分类下存在子分类，不允许删除"),
    PARENT_CATEGORY_MUST_BE_LEVEL1(50040006, "父分类必须是一级分类"),
    CATEGORY_IS_LEAF_ONLY(50040007, "交易只能关联二级分类"),

    BUDGET_AMOUNT_INVALID(60050001, "预算金额必须大于0"),
    BUDGET_TYPE_INVALID(60050002, "预算类型不合法");

    private final Integer code;
    private final String message;

}
