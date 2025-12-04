package com.allen.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseResult<Object>> handleBusinessException(BusinessException e) {
        logger.warning("业务异常: " + e.getMessage());
        ResponseResult<Object> result = ResponseUtils.fail(e.getMessage(), e.getErrorCode());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseResult<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.warning("参数类型错误: " + e.getMessage());
        String errorMsg = "参数类型错误: " + e.getName() + " 应类型为 " + e.getRequiredType().getSimpleName();
        ResponseResult<Object> result = ResponseUtils.fail(errorMsg, ErrorCode.PARAM_ERROR);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseResult<Object>> handleNotFoundException(NoHandlerFoundException e) {
        logger.warning("资源不存在: " + e.getMessage());
        ResponseResult<Object> result = ResponseUtils.fail("请求的资源不存在", ErrorCode.RESOURCE_NOT_FOUND);
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseResult<Object>> handleRuntimeException(RuntimeException e) {
        logger.severe("运行时异常: " + e.getMessage());
        e.printStackTrace();
        ResponseResult<Object> result = ResponseUtils.fail("服务器内部错误", ErrorCode.SYSTEM_ERROR);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Object>> handleAllException(Exception e, HttpServletRequest request) {
        logger.severe("未捕获的异常: " + e.getMessage());
        e.printStackTrace();
        ResponseResult<Object> result = ResponseUtils.fail("服务器内部错误", ErrorCode.SYSTEM_ERROR);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}