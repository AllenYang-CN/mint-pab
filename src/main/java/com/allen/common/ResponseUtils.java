package com.allen.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 响应工具类，提供各种响应结果的静态方法
 */
public class ResponseUtils {
    
    /**
     * 成功响应 - 带数据
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(true, "操作成功", data, null);
    }
    
    /**
     * 成功响应 - 带自定义消息和数据
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(true, message, data, null);
    }
    
    /**
     * 成功响应 - 无数据
     */
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(true, "操作成功", null, null);
    }
    
    /**
     * 成功响应 - 只带消息
     */
    public static <T> ResponseResult<T> success(String message) {
        return new ResponseResult<>(true, message, null, null);
    }
    
    /**
     * 失败响应 - 带错误消息
     */
    public static <T> ResponseResult<T> fail(String message) {
        return new ResponseResult<>(false, message, null, null);
    }
    
    /**
     * 失败响应 - 带错误消息和错误码
     */
    public static <T> ResponseResult<T> fail(String message, String errorCode) {
        return new ResponseResult<>(false, message, null, errorCode);
    }
    
    /**
     * 失败响应 - 带错误消息和数据
     */
    public static <T> ResponseResult<T> fail(String message, T data) {
        return new ResponseResult<>(false, message, data, null);
    }
    
    /**
     * 成功响应 - ResponseEntity封装
     */
    public static <T> ResponseEntity<ResponseResult<T>> ok(T data) {
        return new ResponseEntity<>(success(data), HttpStatus.OK);
    }
    
    /**
     * 成功响应 - 带自定义消息的ResponseEntity封装
     */
    public static <T> ResponseEntity<ResponseResult<T>> ok(String message, T data) {
        return new ResponseEntity<>(success(message, data), HttpStatus.OK);
    }
    
    /**
     * 成功响应 - 只带消息的ResponseEntity封装
     */
    public static <T> ResponseEntity<ResponseResult<T>> ok(String message) {
        return new ResponseEntity<>(success(message), HttpStatus.OK);
    }
    
    /**
     * 无内容响应
     */
    public static <T> ResponseEntity<ResponseResult<T>> noContent() {
        return new ResponseEntity<>(success(), HttpStatus.NO_CONTENT);
    }
    
    /**
     * 错误响应 - 带HTTP状态码
     */
    public static <T> ResponseEntity<ResponseResult<T>> error(HttpStatus status, String message) {
        return new ResponseEntity<>(fail(message), status);
    }
    
    /**
     * 错误响应 - 带HTTP状态码和错误码
     */
    public static <T> ResponseEntity<ResponseResult<T>> error(HttpStatus status, String message, String errorCode) {
        return new ResponseEntity<>(fail(message, errorCode), status);
    }
    
    /**
     * 错误响应 - 400 Bad Request
     */
    public static <T> ResponseEntity<ResponseResult<T>> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * 错误响应 - 404 Not Found
     */
    public static <T> ResponseEntity<ResponseResult<T>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }
    
    /**
     * 错误响应 - 500 Internal Server Error
     */
    public static <T> ResponseEntity<ResponseResult<T>> serverError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}