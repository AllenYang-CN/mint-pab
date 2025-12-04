package com.allen.common;

/**
 * 错误码常量类
 */
public class ErrorCode {
    // 通用错误码
    public static final String SUCCESS = "000000";
    public static final String SYSTEM_ERROR = "999999";
    public static final String PARAM_ERROR = "000001";
    public static final String RESOURCE_NOT_FOUND = "000002";
    public static final String DUPLICATE_RESOURCE = "000003";
    
    // 标签相关错误码
    public static final String TAG_NAME_EXIST = "100001";
    public static final String TAG_NOT_EXIST = "100002";
    public static final String TAG_DELETE_FAILED = "100003";
    public static final String TAG_UPDATE_FAILED = "100004";
    
    // 其他业务模块错误码可以在这里扩展
}