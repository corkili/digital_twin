package com.digitaltwin.websocket.exception;

import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 全局异常处理类
 * 统一处理系统中的异常，确保返回友好的错误信息
 */
@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 处理权限相关的异常
     */
    @ExceptionHandler(SecurityException.class)
    public final ResponseEntity<WebSocketResponse<String>> handleSecurityException(SecurityException ex, WebRequest request) {
        log.error("权限错误: {}", ex.getMessage());
        WebSocketResponse<String> response = WebSocketResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * 处理参数相关的异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<WebSocketResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("参数错误: {}", ex.getMessage());
        WebSocketResponse<String> response = WebSocketResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理其他所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<WebSocketResponse<String>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("系统错误: {}", ex.getMessage(), ex);
        WebSocketResponse<String> response = WebSocketResponse.error("系统内部错误，请稍后再试");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}